package altclasses.case1;

class LionBad {
    private String name;
    private int age;
    public LionBad(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void lionVoice() {
        System.out.println("Roar");
    }
    public void lionInfo() {
        System.out.println("Name: " + name + ", Age: " + age);
    }
}

class RabbitBad {
    private String name;
    private int age;
    public RabbitBad(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void rabbitVoice() {
        System.out.println("Squeak");
    }
    public void rabbitInfo() {
        System.out.println("Name: " + name + ", Age: " + age);
    }
}

public class UserManager {
    private Database db;
    private PdfService pdfService;

    public void changePassword(String userId, String newHash) {
        db.updatePassword(userId, newHash);
    }

    public void generateMonthlyInvoice(String userId, double amount) {
        pdfService.createInvoice(userId, amount);
    }
}