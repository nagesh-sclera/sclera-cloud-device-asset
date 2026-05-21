package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dockerrepo")
public class DockerRepoController {

    @GetMapping("/getAllNetworksByNetworkOrigin")
    public List<String> getAllNetworksByNetworkOrigin(
            @RequestParam(required = false) String networkOrigin) {
        return Defaults.emptyList();
    }
}
