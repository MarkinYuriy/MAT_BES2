package mat.sn;

public class Test {
    public static void main(String[] args) {
        Google g = new Google();
        String[] s = g.getContacts("myavailabletime@gmail.com", "theworldismine");
        for (String str: s) {
            System.out.println(str);
        }
    }
}
