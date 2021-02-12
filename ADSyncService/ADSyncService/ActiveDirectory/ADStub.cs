﻿using System.Collections.Generic;
using System.DirectoryServices;
using System.DirectoryServices.AccountManagement;

namespace ADSyncService
{
    class ADStub
    {
        private static log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        private static string groupOU = Properties.Settings.Default.CreateDeleteFeature_OU;
        private static string cprAttribute = Properties.Settings.Default.MembershipSyncFeature_CprAttribute;

        public List<string> GetGroupMembers(string groupId)
        {
            List<string> members = new List<string>();

            using (PrincipalContext context = new PrincipalContext(ContextType.Domain))
            {
                using (GroupPrincipal group = GroupPrincipal.FindByIdentity(context, groupId))
                {
                    if (group == null)
                    {
                        log.Error("Group does not exist: " + groupId);
                        return null;
                    }
                    else
                    {
                        // argument to GetMembers indicate we want direct members, not indirect members
                        foreach (Principal member in group.GetMembers(false))
                        {
                            members.Add(member.SamAccountName.ToLower());
                        }
                    }
                }
            }

            return members;
        }

        public List<Group> GetAllGroups(string ouDn)
        {
            List<Group> res = new List<Group>();

            using (PrincipalContext context = new PrincipalContext(ContextType.Domain, null, ouDn))
            {
                GroupPrincipal template = new GroupPrincipal(context);
                using (PrincipalSearcher searcher = new PrincipalSearcher(template))
                {
                    ((DirectorySearcher)searcher.GetUnderlyingSearcher()).SearchScope = SearchScope.OneLevel;

                    using (var result = searcher.FindAll())
                    {
                        foreach (var group in result)
                        {
                            GroupPrincipal groupPrincipal = group as GroupPrincipal;
                            if (groupPrincipal != null)
                            {
                                Group g = new Group();
                                g.Uuid = groupPrincipal.Guid.ToString().ToLower();
                                g.Name = groupPrincipal.Name;

                                res.Add(g);
                            }
                        }
                    }
                }
            }

            return res;
        }

        public class Group
        {
            public string Uuid { get; set; }
            public string Name { get; set; }
        }

        public void AddMember(string groupId, string userId)
        {
            using (PrincipalContext context = new PrincipalContext(ContextType.Domain))
            {
                using (UserPrincipal user = UserPrincipal.FindByIdentity(context, userId))
                {
                    using (GroupPrincipal group = GroupPrincipal.FindByIdentity(context, groupId))
                    {
                        if (user == null || group == null)
                        {
                            log.Error("User or group does not exist: " + userId + " / " + groupId);
                            return;
                        }

                        log.Info("Added " + userId + " to " + group.Name);

                        group.Members.Add(user);
                        group.Save();
                    }
                }
            }
        }

        public void RemoveMember(string groupId, string userId)
        {
            using (PrincipalContext context = new PrincipalContext(ContextType.Domain))
            {
                using (UserPrincipal user = UserPrincipal.FindByIdentity(context, userId))
                {
                    using (GroupPrincipal group = GroupPrincipal.FindByIdentity(context, groupId))
                    {
                        if (user == null || group == null)
                        {
                            log.Error("User or group does not exist: " + userId + " / " + groupId);
                            return;
                        }

                        if (group.Members.Remove(user))
                        {
                            log.Info("Removed " + userId + " from " + group.Name);
                            group.Save();
                        }
                        else
                        {
                            log.Warn("Could not remove " + userId + " from " + group.Name);
                        }
                    }
                }
            }
        }

        public void CreateGroup(string systemRoleIdentifier, string itSystemIdentifier)
        {
            string contextPath = "OU=" + itSystemIdentifier + "," + groupOU;

            // make sure contextPath exists
            if (!DirectoryEntry.Exists("LDAP://" + contextPath))
            {
                using (var de = new DirectoryEntry("LDAP://" + groupOU))
                {
                    using (DirectoryEntry child = de.Children.Add("OU=" + itSystemIdentifier, "OrganizationalUnit"))
                    {
                        child.CommitChanges();
                        log.Info("Created OU: " + contextPath);
                    }
                }
            }

            using (PrincipalContext context = new PrincipalContext(ContextType.Domain, null, contextPath))
            {
                using (GroupPrincipal group = GroupPrincipal.FindByIdentity(context, systemRoleIdentifier))
                {
                    if (group == null)
                    {
                        using (var groupPrincipal = new GroupPrincipal(context, systemRoleIdentifier))
                        {
                            groupPrincipal.Save();
                            log.Info("Created security group: " + systemRoleIdentifier + " in " + contextPath);
                        }
                    }
                    else
                    {
                        log.Warn("Could not create new security group " + systemRoleIdentifier + " because it already exists: " + group.DistinguishedName);
                    }
                }
            }
        }

        public bool HasCpr(string userId)
        {
            using (PrincipalContext context = new PrincipalContext(ContextType.Domain))
            {
                using (UserPrincipal user = UserPrincipal.FindByIdentity(context, userId))
                {
                    using (DirectoryEntry de = user.GetUnderlyingObject() as DirectoryEntry)
                    {
                        if (de != null)
                        {
                            var cpr = de.Properties[cprAttribute].Value as string;

                            if (!string.IsNullOrEmpty(cpr) && cpr.Length >= 10 && cpr.Length <= 11)
                            {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }

        public void DeleteGroup(string systemRoleIdentifier, string itSystemIdentifier)
        {
            string contextPath = "OU=" + itSystemIdentifier + "," + groupOU;

            using (PrincipalContext context = new PrincipalContext(ContextType.Domain, null, contextPath))
            {
                using (GroupPrincipal group = GroupPrincipal.FindByIdentity(context, systemRoleIdentifier))
                {
                    if (group != null)
                    {
                        group.Delete();

                        log.Info("Deleted security group: " + systemRoleIdentifier + " in " + contextPath);
                    }
                }
            }
        }
    }
}
