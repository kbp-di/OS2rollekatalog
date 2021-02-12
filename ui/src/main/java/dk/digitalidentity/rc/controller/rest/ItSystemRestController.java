package dk.digitalidentity.rc.controller.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dk.digitalidentity.rc.config.Constants;
import dk.digitalidentity.rc.controller.rest.model.PrefixWrapper;
import dk.digitalidentity.rc.dao.model.ItSystem;
import dk.digitalidentity.rc.dao.model.PendingADGroupOperation;
import dk.digitalidentity.rc.dao.model.SystemRole;
import dk.digitalidentity.rc.dao.model.SystemRoleAssignment;
import dk.digitalidentity.rc.dao.model.UserRole;
import dk.digitalidentity.rc.dao.model.enums.ItSystemType;
import dk.digitalidentity.rc.security.RequireAdministratorRole;
import dk.digitalidentity.rc.security.SecurityUtil;
import dk.digitalidentity.rc.service.ItSystemService;
import dk.digitalidentity.rc.service.OrgUnitService;
import dk.digitalidentity.rc.service.PendingADUpdateService;
import dk.digitalidentity.rc.service.PositionService;
import dk.digitalidentity.rc.service.SystemRoleService;
import dk.digitalidentity.rc.service.UserRoleService;
import dk.digitalidentity.rc.service.UserService;
import lombok.extern.log4j.Log4j;

@Log4j
@RequireAdministratorRole
@RestController
public class ItSystemRestController {

	@Autowired
	private ItSystemService itSystemService;

	@Autowired
	private SystemRoleService systemRoleService;
	
	@Autowired
	private UserRoleService userRoleService;
		
	@Autowired
	private PendingADUpdateService pendingADUpdateService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrgUnitService orgUnitService;

	@Autowired
	private PositionService positionService;

	@PostMapping(value = { "/rest/systemrole/delete/{id}" })
	@ResponseBody
	public HttpEntity<String> deleteSystemRole(Model model, @PathVariable("id") long id) {
		SystemRole systemRole = systemRoleService.getById(id);
		if (systemRole == null || (!systemRole.getItSystem().getSystemType().equals(ItSystemType.AD) && !systemRole.getItSystem().getSystemType().equals(ItSystemType.SAML))) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		List<UserRole> userRoles = userRoleService.getByItSystem(systemRole.getItSystem());
		if (userRoles != null) {
			for (UserRole userRole : userRoles) {
				for (SystemRoleAssignment assignment : userRole.getSystemRoleAssignments()) {
					if (assignment.getSystemRole().equals(systemRole)) {
						return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
					}
				}
			}
		}

		systemRoleService.delete(systemRole);

		if (systemRole.getItSystem().getSystemType().equals(ItSystemType.AD)) {
			PendingADGroupOperation operation = new PendingADGroupOperation();
			operation.setActive(false);
			operation.setItSystemIdentifier(systemRole.getItSystem().getIdentifier());
			operation.setSystemRoleId(null);
			operation.setSystemRoleIdentifier(systemRole.getIdentifier());
			operation.setTimestamp(new Date());

			pendingADUpdateService.save(operation);
		}
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
    @PostMapping(value = "/rest/itsystem/delete/{id}")
    public ResponseEntity<String> deleteItSystemAsync(@PathVariable("id") long id) {
        ItSystem itSystem = itSystemService.getById(id);
        if (itSystem == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        log.info("Deleting it-system " + itSystem.getName() + " with id " + itSystem.getId());

        if (itSystem.getSystemType().equals(ItSystemType.KOMBIT) ||
        	itSystem.getSystemType().equals(ItSystemType.KSPCICS) ||
        	itSystem.getIdentifier().equals(Constants.ROLE_CATALOGUE_IDENTIFIER)) {
        	return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        // delete itsystem
        itSystem.setDeleted(true);
        itSystem.setDeletedTimestamp(new Date());
        itSystemService.save(itSystem);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/rest/itsystem/name")
    public ResponseEntity<String> editItSystemName(long id, String name) {
    	if (name == null || name.length() < 2) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    
    	ItSystem itSystem = itSystemService.getById(id);
    	if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    	
    	ItSystem existingSystem = itSystemService.getFirstByName(name);
    	if (existingSystem != null && existingSystem.getId() != itSystem.getId()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
    	
    	itSystem.setName(name);
    	itSystemService.save(itSystem);

        return new ResponseEntity<>(HttpStatus.OK);
    }

	@PostMapping(value = "/rest/itsystem/email")
	public ResponseEntity<String> editItSystemEmail(long id, String email) {
		if (email == null || email.length() < 2) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		ItSystem itSystem = itSystemService.getById(id);
		if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		itSystem.setEmail(email);
		itSystemService.save(itSystem);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@PostMapping(value = "/rest/itsystem/notificationemail")
	public ResponseEntity<String> editItSystemnNotificationEmail(long id, String email) {
		ItSystem itSystem = itSystemService.getById(id);
		if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		itSystem.setNotificationEmail(email);
		itSystemService.save(itSystem);

		return new ResponseEntity<>(HttpStatus.OK);
	}
    
    @PostMapping(value = "/rest/itsystem/notes")
    public ResponseEntity<String> editItSystemNotes(long id, String notes) {
    	ItSystem itSystem = itSystemService.getById(id);
    	if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    	
    	itSystem.setNotes(notes);
    	itSystemService.save(itSystem);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    @PostMapping(value = "/rest/itsystem/paused")
    public ResponseEntity<String> editItSystemPaused(long id, boolean paused) {
    	ItSystem itSystem = itSystemService.getById(id);
    	if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    	
    	itSystem.setPaused(paused);
    	itSystemService.save(itSystem);

    	if (!paused) {
    		// if the pause flag is removed, add the full it-system
    		// to the queue for synchronization
    		pendingADUpdateService.addItSystemToQueue(itSystem);
    	}
    	else {
    		pendingADUpdateService.removeItSystemFromQueue(itSystem);
    	}
    	
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/rest/itsystem/canEditThroughApi")
    public ResponseEntity<String> editItSystemCanEditThroughApi(long id, boolean canEditThroughApi) {
    	ItSystem itSystem = itSystemService.getById(id);
    	if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (itSystem.getSystemType() != ItSystemType.AD && itSystem.getSystemType() != ItSystemType.SAML) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

    	itSystem.setCanEditThroughApi(canEditThroughApi);
    	itSystemService.save(itSystem);

        return new ResponseEntity<>(HttpStatus.OK);
    }

	@PostMapping(value = "/rest/itsystem/hidden")
	public ResponseEntity<String> editItSystemHidden(long id, boolean hidden) {
		ItSystem itSystem = itSystemService.getById(id);
		if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		itSystem.setHidden(hidden);
		itSystemService.save(itSystem);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/rest/itsystem/subscribedTo")
	public ResponseEntity<String> editItSystemSubscribedTo(long id, String masterId) {
		ItSystem itSystem = itSystemService.getById(id);
		if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// quick'n'dirty workaround for Javascript and null values
		if (masterId.equals("null")) {
			masterId = null;
		}

		itSystem.setSubscribedTo(masterId);
		itSystemService.save(itSystem);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	// TODO: really should use the count() method instead
	@SuppressWarnings("deprecation")
	@PostMapping(value = "/rest/itsystem/userrole/unused/{id}")
	public ResponseEntity<String> deleteUnusedUserRoles(@PathVariable("id") long id) {
		ItSystem itSystem = itSystemService.getById(id);
		if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (itSystem.getSystemType().equals(ItSystemType.KSPCICS)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// delete unused user roles
		List<UserRole> userRoles = userRoleService.getByItSystem(itSystem);
		for (UserRole userRole : userRoles) {
			// check if assigned to user
			if (userService.countAllWithRole(userRole) > 0) {
				continue;
			}

			// check if assigned to orgUnit
			if (orgUnitService.countAllWithRole(userRole) > 0) {
				continue;
			}
			
			// check if assigned to position
			if (positionService.getAllWithRole(userRole).size() > 0) {
				continue;
			}

			userRoleService.delete(userRole);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/rest/itsystem/systemrole/convert/{id}")
	public ResponseEntity<String> convertSystemRoles(@PathVariable("id") long id, @RequestBody PrefixWrapper prefixWrapper) {
		ItSystem itSystem = itSystemService.getById(id);
		if (itSystem == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (itSystem.getSystemType().equals(ItSystemType.KSPCICS)) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		List<SystemRole> systemRoles = systemRoleService.getByItSystem(itSystem).stream()
				.filter(sr -> systemRoleService.isInUse(sr) == false)
				.collect(Collectors.toList());

		for (SystemRole systemRole : systemRoles) {
			UserRole userRole = new UserRole();
			userRole.setName(prefixWrapper.getPrefix() + systemRole.getName());
			userRole.setDescription(systemRole.getDescription());
			userRole.setIdentifier("id-" + UUID.randomUUID().toString());
			userRole.setItSystem(itSystem);
			userRole.setSystemRoleAssignments(new ArrayList<SystemRoleAssignment>());
			userRole = userRoleService.save(userRole);

			SystemRoleAssignment roleAssignment = new SystemRoleAssignment();
			roleAssignment.setSystemRole(systemRole);
			roleAssignment.setUserRole(userRole);
			roleAssignment.setAssignedByName(SecurityUtil.getUserFullname());
			roleAssignment.setAssignedByUserId(SecurityUtil.getUserId());
			roleAssignment.setAssignedTimestamp(new Date());

			userRoleService.addSystemRoleAssignment(userRole, roleAssignment);
			userRoleService.save(userRole);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
