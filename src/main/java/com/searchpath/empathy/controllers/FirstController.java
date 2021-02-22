package com.searchpath.empathy.controllers;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/search")
public class FirstController {

    @Get(produces = MediaType.TEXT_PLAIN)
    public String search() {
        return "Hello Empathy!";
    }

}
