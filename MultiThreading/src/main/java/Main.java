public class Main {
    private static final String str = "brow \n what? \n are\n ya";
    private static final String midStr = "brow \n what? \n are\n ya \n brow \n what? \n are\n ya";
    private static final String mid2Str = "brow brow \n what? what? \n are are are are are are\n ya \n browbrowbrow browbrow brow  \n what? \n are\n ya";
    private static final String longStr = "brow \n what? \n are\n ya \n brow \n what? \n are\n ya \n brow \n what? \n are\n ya \n brow \n what? \n are\n ya";
    private static final String long2Str = longStr + longStr + longStr + longStr;
    private static final String long4Str = long2Str + long2Str + long2Str + long2Str;
    private static final String long8Str = long4Str + long4Str + long4Str + long4Str;

    public static void main(String[] args) throws InterruptedException {
        Master master = new Master(long8Str);
        System.out.println(master.getWordsFrequency());
    }
}
