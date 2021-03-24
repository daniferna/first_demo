package com.searchpath.empathy.elastic.commands;

import java.io.IOException;

public interface Command {

    void execute(Object[] args) throws IOException;

}
