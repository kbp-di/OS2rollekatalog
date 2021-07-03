package dk.digitalidentity.rc.controller.mvc;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dk.digitalidentity.rc.config.RoleCatalogueConfiguration;
import dk.digitalidentity.rc.controller.mvc.viewmodel.Assignment;
import dk.digitalidentity.rc.controller.mvc.viewmodel.EditUserRoleRow;
import dk.digitalidentity.rc.controller.mvc.viewmodel.RequestForm;
import dk.digitalidentity.rc.controller.mvc.viewmodel.RoleGroupForm;
import dk.digitalidentity.rc.controller.mvc.viewmodel.RolesOUTable;
import dk.digitalidentity.rc.controller.mvc.viewmodel.TitleListForm;
import dk.digitalidentity.rc.controller.mvc.viewmodel.UserRoleCheckedDTO;
import dk.digitalidentity.rc.controller.validator.RolegroupValidator;
import dk.digitalidentity.rc.dao.model.OrgUnit;
import dk.digitalidentity.rc.dao.model.RoleGroup;
import dk.digitalidentity.rc.dao.model.Title;
import dk.digitalidentity.rc.dao.model.User;
import dk.digitalidentity.rc.dao.model.UserRole;
import dk.digitalidentity.rc.security.RequireAdministratorRole;
import dk.digitalidentity.rc.security.RequireRequesterOrReadAccessRole;
import dk.digitalidentity.rc.security.SecurityUtil;
import dk.digitalidentity.rc.service.OrgUnitService;
import dk.digitalidentity.rc.service.RequestApproveService;
import dk.digitalidentity.rc.service.RoleGroupService;
import dk.digitalidentity.rc.service.SettingsService;
import dk.digitalidentity.rc.service.UserRoleService;
import dk.digitalidentity.rc.service.UserService;
import dk.digitalidentity.rc.service.model.OrgUnitWithRole;
import dk.digitalidentity.rc.service.model.RoleWithDateDTO;
import dk.digitalidentity.rc.service.model.UserWithRole;
import dk.digitalidentity.rc.service.model.UserWithRoleAndDates;

@RequireRequesterOrReadAccessRole
@Controller
public class RoleGroupController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private UserRoleService userRoleService;

	@Autowired
	private RoleGroupService roleGroupService;

	@Autowired
	private RolegroupValidator rolegroupValidator;

	@Autowired
	private UserService userService;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private RequestApproveService requestApproveService;

	@Autowired
	private OrgUnitService orgUnitService;
	
	@Autowired
    private RoleCatalogueConfiguration configuration;

	@InitBinder(value = { "rolegroup" })
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(rolegroupValidator);
	}

	@GetMapping(value = { "/ui/rolegroups/list" })
	public String list(Model model, Principal principal) throws Exception {
		List<RoleGroup> roles = roleGroupService.getAll();

		// requesters needs to have the list filtered
		if (SecurityUtil.isRequesterAndOnlyRequester()) {
			User user = getUserOrThrow(principal.getName());

			roles = roleGroupService.whichRolesCanBeRequestedByUser(roles, user);
		}

		model.addAttribute("roleGroups", roles);

		return "rolegroups/list";
	}

	@GetMapping(value = "/ui/rolegroups/view/{id}")
	public String view(Model model, @PathVariable("id") long id, Principal principal) throws Exception {
		RoleGroup roleGroup = roleGroupService.getById(id);
		if (roleGroup == null) {
			return "redirect:../list";
		}

		RoleGroupForm roleGroupForm = mapper.map(roleGroup, RoleGroupForm.class);
		
		// remove userRoles from soft-deleted it-systems until they are really deleted
		roleGroupForm.setUserRoleAssignments(roleGroup.getUserRoleAssignments().stream()
				.filter(a -> a.getUserRole().getItSystem().isDeleted() == false)
				.collect(Collectors.toList()));

		model.addAttribute("rolegroup", roleGroupForm);

		boolean canRequest = false;
		if (settingsService.isRequestApproveEnabled()) {
			User user = getUserOrThrow(principal.getName());
			canRequest = roleGroupService.canRequestRole(roleGroup, user);
		}
		model.addAttribute("canRequest", canRequest);
		model.addAttribute("requestForm", new RequestForm(id));

		List<UserWithRole> mapping = userService.getUsersWithRoleGroup(roleGroup, true);
		model.addAttribute("userRoleMapping", mapping);

		List<OrgUnitWithRole> orgUnits = orgUnitService.getOrgUnitsWithRoleGroup(roleGroup, true);
		model.addAttribute("orgUnits", orgUnits);

		return "rolegroups/view";
	}

	@PostMapping(value = "/ui/rolegroups/request")
	public String requestRole(Model model, RequestForm requestForm, RedirectAttributes redirectAttributes, Principal principal) throws Exception {
		RoleGroup roleGroup = roleGroupService.getById(requestForm.getId());
		Locale locale = LocaleContextHolder.getLocale();
		User user = getUserOrThrow(principal.getName());

		if (!requestApproveService.requestRoleGroup(roleGroup, user, requestForm.getReason())) {
			redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("html.page.roles.request.error", null, locale));
		}
		else {
			redirectAttributes.addFlashAttribute("infoMessage", messageSource.getMessage("html.page.roles.request.send", null, locale));
		}

		return "redirect:/ui/rolegroups/view/" + requestForm.getId();
	}

	@RequireAdministratorRole
	@GetMapping(value = "/ui/rolegroups/edit/{id}")
	public String edit(Model model, @PathVariable("id") long id) {
		RoleGroup roleGroup = roleGroupService.getById(id);
		if (roleGroup == null) {
			return "redirect:../list";
		}

		RoleGroupForm roleGroupForm = mapper.map(roleGroup, RoleGroupForm.class);
		boolean titlesEnabled = configuration.getTitles().isEnabled();
		
		List<User> usersFromDb = userService.getAll();
		List<UserWithRoleAndDates> usersWithRole = userService.getUsersWithRoleGroupDirectlyAssigned(roleGroup);
		List<String> uuidsWithRole = usersWithRole.stream().map(u -> u.getUser().getUuid()).collect(Collectors.toList());
		List<UserRoleCheckedDTO> users = new ArrayList<>();
		
		for (User user : usersFromDb) {
			LocalDate startDate = null;
			LocalDate stopDate = null;
			boolean checked = false;
			
			if (uuidsWithRole.contains(user.getUuid())) {
				checked = true;
				//We know it exists because of the if, and there should only be one
				UserWithRoleAndDates userWithRole = usersWithRole.stream().filter(u -> u.getUser().getUuid().equals(user.getUuid())).findAny().orElse(null);
				startDate = userWithRole.getStartDate();
				stopDate = userWithRole.getStopDate();
			}
			
			UserRoleCheckedDTO dto = new UserRoleCheckedDTO();
			dto.setName(user.getName());
			dto.setUuid(user.getUuid());
			dto.setUserId(user.getUserId());
			dto.setChecked(checked);
			dto.setStartDate(startDate);
			dto.setStopDate(stopDate);
			
			users.add(dto);
		}
		
		List<OrgUnit> oUsFromDb = orgUnitService.getAll();
		List<RolesOUTable> allOUs = new ArrayList<>();
		for (OrgUnit ou : oUsFromDb) {
			Assignment a = new Assignment();
			List<RoleWithDateDTO> rolegroups = orgUnitService.getNotInheritedRoleGroupsWithDate(ou);
			RoleWithDateDTO rg = rolegroups.stream().filter(r -> r.getId() == roleGroup.getId()).findFirst().orElse(null);
			if (rg != null) {
				a.setStartDate(rg.getStartDate());
				a.setStopDate(rg.getStopDate());
				allOUs.add(new RolesOUTable(ou, true, a));
			}else {
				allOUs.add(new RolesOUTable(ou, false, a));
			}
		}

		model.addAttribute("rolegroup", roleGroupForm);
		model.addAttribute("roles", getAddRoles(roleGroup));
		model.addAttribute("users", users);
		model.addAttribute("allOUs", allOUs);
		model.addAttribute("titlesEnabled", titlesEnabled);

		return "rolegroups/edit";
	}

	@RequireAdministratorRole
	@GetMapping(value = "/ui/rolegroups/new")
	public String newGet(Model model) {
		model.addAttribute("rolegroup", new RoleGroupForm());

		return "rolegroups/new";
	}

	@RequireAdministratorRole
	@PostMapping(value = "/ui/rolegroups/new")
	public String newPost(Model model, @Valid @ModelAttribute("rolegroup") RoleGroupForm rolegroupForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			model.addAllAttributes(bindingResult.getModel());
			model.addAttribute("rolegroup", rolegroupForm);
			return "rolegroups/new";
		}

		RoleGroup roleGroup = mapper.map(rolegroupForm, RoleGroup.class);

		roleGroup = roleGroupService.save(roleGroup);
		return "redirect:../edit/" + roleGroup.getId();
	}
	
	@GetMapping(value = "/ui/rolegroups/fragments/{uuid}")
	public String getFragment(Model model, @PathVariable("uuid") String uuid) {
		User user = userService.getByUuid(uuid);
		
		if (user != null) {
			model.addAttribute("positions", user.getPositions());
		}
		
		return "users/fragments/user_role_group_modal :: userRoleGroupModal";
		
	}
	
	@GetMapping(value = "/ui/rolegroups/fragments/ou/{uuid}")
	public String getOUFragment(Model model, @PathVariable("uuid") String uuid) {
		OrgUnit orgUnit = orgUnitService.getByUuid(uuid);
		boolean titlesEnabled = configuration.getTitles().isEnabled();
		
		if (titlesEnabled && orgUnit != null) {
			List<Title> titles = orgUnitService.getTitles(orgUnit);

			List<TitleListForm> titleForms = titles
					.stream()
					.map(title -> new TitleListForm(title))
					.collect(Collectors.toList());
			
			model.addAttribute("titles", titleForms);
		}
		else {
			model.addAttribute("titles", null);
		}
		
		model.addAttribute("titlesEnabled", titlesEnabled);
		return "ous/fragments/ou_roles_modal :: ouRolesModal";
		
	}

	private List<EditUserRoleRow> getAddRoles(RoleGroup rolegroup) {
		List<EditUserRoleRow> addRoles = new ArrayList<>();

		// note that we filter out roles that are part of the role catalogue, as
		// they should never be in a role group
		for (UserRole role : userRoleService.getAllExceptRoleCatalogue()) {
			// filter roles from deleted itSystems
			if (role.getItSystem().isDeleted()) {
				continue;
			}

			UserRole newRole = new UserRole();
			newRole.setDescription(role.getDescription());
			newRole.setId(role.getId());
			newRole.setIdentifier(role.getIdentifier());
			newRole.setItSystem(role.getItSystem());
			newRole.setName(role.getName());
			newRole.setSystemRoleAssignments(role.getSystemRoleAssignments());

			EditUserRoleRow roleWithAssignment = new EditUserRoleRow();
			roleWithAssignment.setRole(newRole);

			if (rolegroup.getUserRoleAssignments().stream().map(ura -> ura.getUserRole()).collect(Collectors.toList()).contains(role)) {
				roleWithAssignment.setChecked(true);
			}
			else {
				roleWithAssignment.setChecked(false);
			}

			addRoles.add(roleWithAssignment);
		}

		return addRoles;
	}

	private User getUserOrThrow(String userId) throws Exception {
		User user = userService.getByUserId(userId);
		if (user == null) {
			throw new Exception("Ukendt bruger: " + userId);
		}

		return user;
	}
}
