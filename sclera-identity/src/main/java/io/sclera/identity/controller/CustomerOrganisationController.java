package io.sclera.identity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  @PostMapping("/upsertCustomerByOrganisationIdSync")
  public void upsertCustomerByOrganisationIdSync(@RequestParam(required=false) String orgId) {
    // no-op
  }

  @PostMapping("/deleteCustomerOrgById")
  public void deleteCustomerOrgById(@RequestParam(required=false) String orgId) {
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

  @PostMapping("/insertUser")
  public void insertUser(@RequestBody String creation_timestamp, @RequestParam(required=false) String email, @RequestParam(required=false) String company_name, @RequestParam(required=false) String created_by, @RequestParam(required=false) String name, @RequestParam(required=false) String phone, @RequestParam(required=false) String phone_type, @RequestParam(required=false) String value, @RequestParam(required=false) String website, @RequestParam(required=false) String organisation_id, @RequestParam(required=false) String image_url, @RequestParam(required=false) String language, @RequestParam(required=false) String role) {
    // no-op
  }

  @PostMapping("/updateUser")
  public void updateUser(@RequestParam(required=false) String company_name, @RequestParam(required=false) String created_by, @RequestParam(required=false) String name, @RequestParam(required=false) String phone, @RequestParam(required=false) String phone_type, @RequestParam(required=false) String value, @RequestParam(required=false) String website, @RequestParam(required=false) String organisation_id, @RequestParam(required=false) String email) {
    // no-op
  }

  @PostMapping("/deleteUsersByOrganisationId")
  public void deleteUsersByOrganisationId(@RequestParam(required=false) String customer_org_id) {
    // no-op
  }

  @GetMapping("/getAllUsers")
  public Set<String> getAllUsers(@RequestParam Integer pagesize, @RequestParam Integer offset, @RequestParam String searchkey) {
    return Defaults.emptySet();
  }

  @PostMapping("/deleteById")
  public void deleteById(@RequestParam(required=false) String email) {
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

  @PostMapping("/updateAllUser")
  public void updateAllUser(@RequestBody String creation_timestamp, @RequestParam(required=false) String company_name, @RequestParam(required=false) String created_by, @RequestParam(required=false) String name, @RequestParam(required=false) String phone, @RequestParam(required=false) String phone_type, @RequestParam(required=false) String value, @RequestParam(required=false) String website, @RequestParam(required=false) String organisation_id, @RequestParam(required=false) String email, @RequestParam(required=false) String image_url, @RequestParam(required=false) String language, @RequestParam(required=false) String role) {
    // no-op
  }

  @GetMapping("/getAllUsersByOrganisationId")
  public List<String> getAllUsersByOrganisationId(@RequestParam String customer_org_id) {
    return Defaults.emptyList();
  }

  @PostMapping("/updateCustomerOrgIdForUsers")
  public void updateCustomerOrgIdForUsers(@RequestParam(required=false) String existing_customer_org_id, @RequestParam(required=false) String new_customer_org_id) {
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

  @PostMapping("/addVendor")
  public void addVendor(@RequestParam(required=false) String vendorOrgId, @RequestParam(required=false) String vdmsId) {
    // no-op
  }
}
