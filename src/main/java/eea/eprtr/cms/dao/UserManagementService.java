/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Web Transfer 1.0
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Raptis Dimos
 */
package eea.eprtr.cms.dao;

import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;

/**
 * Additional operations for Springs UserDetailsManager.
 */
public interface UserManagementService extends UserDetailsManager, GroupManager {

    /**
     * Returns a list of all users.
     *
     * @return list of users
     */
    List<UserDetails> getAllUsers();

}
