package mat.sn;

public class Test {
    public static void main(String[] args) {
        Google g = new Google();
        String[] s = g.getContacts("gobrol@gmail.com", "born2ki||");
        for (String str: s) {
            System.out.println(str);
        }
    }
}
