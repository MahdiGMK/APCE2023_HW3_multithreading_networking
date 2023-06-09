package Client.View.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Command {
    CREATE_TASK("create task", Arrays.asList(
            new Option("name", 1, true),
            new Option("node", 1, false)
    )),
    GET_TASKS("get tasks", Collections.EMPTY_LIST),
    GET_NODES("get nodes", Collections.EMPTY_LIST),
    DELETE_TASK("delete task", Arrays.asList(
            new Option("name", 1, true)
    )),
    DISABLE_NODE("disable node", Arrays.asList(
            new Option("name", 1, true)
    )),
    ENABLE_NODE("enable node", Arrays.asList(
            new Option("name", 1, true)
    ));

    private final String commandRegex;
    private final List<Option> options;

    Command(String commandRegex, List<Option> options) {
        this.commandRegex = postProcess(commandRegex);
        this.options = options;
    }

    private String postProcess(String str) {
        str = str.replace(" ", "\\s+");
        return str;
    }

    public String getCommandRegex() {
        return commandRegex;
    }

    public List<Option> getOptions() {
        return options;
    }
}
