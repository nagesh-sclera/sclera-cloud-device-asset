package io.sclera.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.identity.defaults.Defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/customerorganisation")
public class CustomerOrganisationController {
  @GetMapping("/upsertCustomerByOrganisationIdSync")
  public void upsertCustomerByOrganisationIdSync(@RequestParam String orgId) {
    // no-op
  }

  @GetMapping("/deleteCustomerOrgById")
  public void deleteCustomerOrgById(@RequestParam String orgId) {
    // no-op
  }

  @GetMapping("/getOrganisationIdByUserEmail")
  public String getOrganisationIdByUserEmail(@RequestParam String email) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/checkUser")
  public int checkUser(@RequestParam String email, @RequestParam String organisation_id) {
    return Defaults.ZERO;
  }

  @GetMapping("/insertUser")
  public void insertUser(@RequestParam String email, @RequestParam String company_name, @RequestParam String created_by, @RequestParam String creation_timestamp, @RequestParam String name, @RequestParam String phone, @RequestParam String phone_type, @RequestParam String value, @RequestParam String website, @RequestParam String organisation_id, @RequestParam String image_url, @RequestParam String language, @RequestParam String role) {
    // no-op
  }

  @GetMapping("/updateUser")
  public void updateUser(@RequestParam String company_name, @RequestParam String created_by, @RequestParam String name, @RequestParam String phone, @RequestParam String phone_type, @RequestParam String value, @RequestParam String website, @RequestParam String organisation_id, @RequestParam String email) {
    // no-op
  }

  @GetMapping("/deleteUsersByOrganisationId")
  public void deleteUsersByOrganisationId(@RequestParam String customer_org_id) {
    // no-op
  }

  @GetMapping("/getAllUsers")
  public Set<String> getAllUsers(@RequestParam Integer pagesize, @RequestParam Integer offset, @RequestParam String searchkey) {
    return Defaults.emptySet();
  }

  @GetMapping("/deleteById")
  public void deleteById(@RequestParam String email) {
    // no-op
  }

  @GetMapping("/editUsers")
  public void editUsers(@RequestParam String company_name, @RequestParam String name, @RequestParam String phone, @RequestParam String phone_type, @RequestParam String value, @RequestParam String website, @RequestParam String email, @RequestParam String language) {
    // no-op
  }

  @GetMapping("/getAllOrganisationUsersByPagination")
  public Set<String> getAllOrganisationUsersByPagination(@RequestParam Integer pagesize, @RequestParam Integer offset, @RequestParam String searchkey, @RequestParam String customer_org_id) {
    return Defaults.emptySet();
  }

  @GetMapping("/getAllOtherUsersByPagination")
  public Set<String> getAllOtherUsersByPagination(@RequestParam Integer pagesize, @RequestParam Integer offset, @RequestParam String searchkey) {
    return Defaults.emptySet();
  }

  @GetMapping("/getUserByEmail")
  public String getUserByEmail(@RequestParam String email) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getAllUsersEmail")
  public Set<String> getAllUsersEmail() {
    return Defaults.emptySet();
  }

  @GetMapping("/updateAllUser")
  public void updateAllUser(@RequestParam String company_name, @RequestParam String created_by, @RequestParam String name, @RequestParam String phone, @RequestParam String phone_type, @RequestParam String value, @RequestParam String website, @RequestParam String organisation_id, @RequestParam String email, @RequestParam String image_url, @RequestParam String language, @RequestParam String role, @RequestParam String creation_timestamp) {
    // no-op
  }

  @GetMapping("/getAllUsersByOrganisationId")
  public List<String> getAllUsersByOrganisationId(@RequestParam String customer_org_id) {
    return Defaults.emptyList();
  }

  @GetMapping("/updateCustomerOrgIdForUsers")
  public void updateCustomerOrgIdForUsers(@RequestParam String existing_customer_org_id, @RequestParam String new_customer_org_id) {
    // no-op
  }

  @GetMapping("/getUsers")
  public Set<String> getUsers() {
    return Defaults.emptySet();
  }

  @GetMapping("/getUserNameByEmail")
  public String getUserNameByEmail(@RequestParam String email) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/getAllUserRoles")
  public String getAllUserRoles(@RequestParam String email) {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/checkUserByEmail")
  public int checkUserByEmail(@RequestParam String email) {
    return Defaults.ZERO;
  }

  @GetMapping("/getMasterUserEmail")
  public String getMasterUserEmail() {
    return Defaults.NULL_STRING;
  }

  @GetMapping("/addVendor")
  public void addVendor(@RequestParam String vendorOrgId, @RequestParam String vdmsId) {
    // no-op
  }
}
