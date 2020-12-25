package alphaComplex.core.gameplay;

import java.awt.image.BufferedImage;

public class Clone {

    private String name;
    private String sector;
    private String gender;
    private String clearance = "I";
    private final String[] personality = new String[3];
    private BufferedImage picture;
    private int cloneID = 1;

    public Clone(String name, String sector, String gender, String[] personality, BufferedImage image) {
        this.name = name;
        this.sector = sector;
        this.gender = gender;
        this.personality[0] = personality[0];
        this.personality[1] = personality[1];
        this.personality[2] = personality[2];
        this.picture = image;
    }

    public String getName() {
        return name + "-" + clearance + "-" + sector + "-" + cloneID;
    }

    public String getGender() {
        return gender;
    }

    public BufferedImage getPicture() {
        return picture;
    }

    public String[] getPersonality() {
        return personality;
    }
}
