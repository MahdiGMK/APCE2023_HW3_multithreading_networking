package Client.View.Command;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {
    private static Pattern getPattern(Command command) {
        String input = "(\\s+[^\"\\s]\\S*)|(\\s+\"[^\"]+\")";
        String optionsRegex = "";
        for (Option option : command.getOptions()) {
            optionsRegex += (optionsRegex.isEmpty() ? "" : "|") + "(\\s+-" + option.getName() +
                    "(" + input + "){" + option.getInputsCount() + "})";
        }
        if (!optionsRegex.isEmpty()) optionsRegex = "(" + optionsRegex + ")*";
        String regex = "\\s*" + command.getCommandRegex() + optionsRegex + "\\s*";
        return Pattern.compile(regex);
    }

    public static TreeMap<String, ArrayList<String>> matches(Command command, String input) {
        if (!getPattern(command).matcher(input).matches()) return null;
        TreeMap<String, ArrayList<String>> map = new TreeMap<>();
        for (Option option : command.getOptions()) {
            String regex = "\\s+-" + option.getName() + "(?<inputs>((\\s+[^\"\\s]\\S*)|(\\s+\"[^\"]+\")){" +
                    option.getInputsCount() + "})";
            Matcher matcher = Pattern.compile(regex).matcher(input);
            if (!matcher.find()) {
                if (option.isRequired()) return null;
                continue;
            }
            Matcher inputsMatcher = Pattern.compile("(?<input>(\\s+[^\"\\s]\\S*)|(\\s+\"[^\"]+\"))").
                    matcher(matcher.group("inputs"));
            ArrayList<String> inputs = new ArrayList<>();
            for (int i = 0; i < option.getInputsCount(); i++) {
                inputsMatcher.find();
                String string = inputsMatcher.group("input").trim();
                inputs.add(string.charAt(0) == '"' ? string.substring(1, string.length() - 1) : string);
            }
            map.put(option.getName(), inputs);
            if (matcher.find()) return null;
        }
        return map;
    }
}
