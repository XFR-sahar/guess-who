package com.example.fixed_guess_who;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataManager {

    public static List<GameCharacter> getCharactersByCategory(String category) {
        List<GameCharacter> characters = new ArrayList<>();


        if ("רגיל".equals(category)) {

            characters.add(createChar("eitan", R.drawable.eitan,
                    "IS_MALE", "SKIN_DARK", "HAS_HAT", "HAS_MUSTACHE", "HAS_COLLAR", "WEARS_ORANGE"));

            characters.add(createChar("amal", R.drawable.amal,
                    "IS_FEMALE", "SKIN_DARK", "HAS_RELIGIOUS_HEADWEAR", "WEARS_RED","WEARS_PURPLE"));

            characters.add(createChar("avi", R.drawable.avi,
                    "IS_MALE","HAS_COLLAR", "HAS_GLASSES", "HAS_RELIGIOUS_HEADWEAR", "WEARS_BLUE"));

            characters.add(createChar("maya", R.drawable.maya,
                    "IS_FEMALE", "HAS_GLASSES", "HAIR_BLACK", "HAS_EARRINGS", "WEARS_ORANGE"));

            characters.add(createChar("raj", R.drawable.raj,
                    "IS_MALE", "SKIN_DARK", "HAS_RELIGIOUS_HEADWEAR","HAS_COLLAR", "HAS_BEARD", "HAS_MUSTACHE", "WEARS_ORANGE","WEARS_GREEN"));

            characters.add(createChar("sam", R.drawable.sam,
                    "IS_MALE", "HAS_HAT","HAS_COLLAR", "HAIR_LIGHT", "WEARS_GREEN"));

            characters.add(createChar("nia", R.drawable.nia,
                    "IS_FEMALE", "SKIN_DARK", "HAIR_CURLY", "HAS_EARRINGS", "WEARS_GREEN", "HAIR_BLACK"));

            characters.add(createChar("joy", R.drawable.joy,
                    "IS_MALE", "HAS_HAT","HAS_COLLAR", "HAS_MUSTACHE", "WEARS_ORANGE"));

            characters.add(createChar("noor", R.drawable.noor,
                    "IS_FEMALE", "SKIN_DARK", "HAS_RELIGIOUS_HEADWEAR", "HAS_EARRINGS","HAS_HOOP_EARRINGS", "WEARS_ORANGE","WEARS_YELLOW"));

            characters.add(createChar("tom", R.drawable.tom,
                    "IS_MALE", "SKIN_DARK", "IS_BALD", "HAS_BEARD", "HAS_MUSTACHE", "WEARS_GREEN"));

            characters.add(createChar("sara", R.drawable.sara,
                    "IS_FEMALE", "HAS_GLASSES", "HAS_EARRINGS","HAS_HOOP_EARRINGS", "WEARS_YELLOW", "HAIR_BLACK"));

            characters.add(createChar("dan", R.drawable.dan,
                    "IS_MALE", "HAS_HAT", "HAS_GLASSES","HAS_COLLAR", "WEARS_GREEN"));

            characters.add(createChar("ron", R.drawable.ron,
                    "IS_MALE", "HAS_GLASSES", "HAIR_CURLY", "HAIR_LIGHT", "HAS_COLLAR", "WEARS_RED", "WEARS_BLUE"));

            characters.add(createChar("lee", R.drawable.lee,
                    "IS_FEMALE", "HAIR_BLACK","HAS_COLLAR", "WEARS_BLUE"));

            characters.add(createChar("eli", R.drawable.eli,
                    "IS_MALE", "SKIN_DARK","HAIR_CURLY", "HAIR_DREADS","HAS_COLLAR", "WEARS_GREEN", "HAIR_BLACK"));

            characters.add(createChar("kim", R.drawable.kim,
                    "IS_FEMALE", "HAS_HAT", "HAS_GLASSES", "HAS_EARRINGS","HAS_HOOP_EARRINGS", "WEARS_RED", "HAIR_BLACK"));

            characters.add(createChar("ema", R.drawable.ema,
                    "IS_FEMALE", "HAIR_BLACK", "HAS_BINDI", "HAS_EARRINGS", "WEARS_BLUE"));

            characters.add(createChar("ben", R.drawable.ben,
                    "IS_MALE", "SKIN_DARK", "HAS_HAT","HAS_COLLAR", "HAS_GLASSES", "WEARS_BLUE"));

            characters.add(createChar("joe", R.drawable.joe,
                    "IS_MALE", "SKIN_DARK","HAS_COLLAR","HAIR_CURLY", "HAIR_BLACK", "WEARS_YELLOW"));

            characters.add(createChar("eve", R.drawable.eve,
                    "IS_FEMALE", "HAIR_LIGHT", "HAS_NECKLACE", "WEARS_PURPLE"));

            characters.add(createChar("ian", R.drawable.ian,
                    "IS_MALE", "SKIN_DARK", "HAS_BEARD", "HAS_COLLAR", "HAS_MUSTACHE", "WEARS_ORANGE", "HAIR_BLACK"));

            characters.add(createChar("nadav", R.drawable.nadav,
                    "IS_MALE", "SKIN_DARK","HAS_COLLAR", "IS_BALD", "HAS_GLASSES", "WEARS_PURPLE"));

            characters.add(createChar("rose", R.drawable.rose,
                    "IS_FEMALE", "HAS_GLASSES", "HAIR_LIGHT","HAS_COLLAR", "WEARS_GREEN"));

            characters.add(createChar("mia", R.drawable.mia,
                    "IS_FEMALE", "SKIN_DARK", "HAIR_BRAIDS", "HAS_EARRINGS", "WEARS_RED", "HAIR_BLACK"));
        }




        else if ("אנימה".equals(category)) {
            // Dragon Ball
            characters.add(createChar("goku", R.drawable.goku, "IS_MALE", "HAIR_BLACK", "SPIKY_HAIR", "WEARS_BRIGHT", "FROM_DBZ", "PROTAGONIST", "CAN_FLY", "IS_SAIYAN", "CAN_TRANSFORM"));
            characters.add(createChar("vegita", R.drawable.vegita, "IS_MALE", "COLORFUL_HAIR", "SPIKY_HAIR", "WEARS_DARK", "FROM_DBZ", "IS_SAIYAN", "CAN_FLY", "CAN_TRANSFORM"));
            characters.add(createChar("bulma", R.drawable.bulma, "IS_FEMALE", "COLORFUL_HAIR", "HAS_CLOAK", "FROM_DBZ", "USES_TECH"));
            characters.add(createChar("piccolo", R.drawable.picolo, "IS_MALE", "NON_HUMAN_SKIN", "IS_BALD", "POINTY_EARS", "HAS_CLOAK", "FROM_DBZ", "CAN_FLY", "CAN_TRANSFORM"));
            // One Piece
            characters.add(createChar("luffy", R.drawable.luffy, "IS_MALE", "HAIR_BLACK", "HAS_HAT", "WEARS_BRIGHT", "FROM_OP", "PROTAGONIST", "IS_PIRATE", "CAN_TRANSFORM"));
            characters.add(createChar("nami", R.drawable.namy, "IS_FEMALE", "COLORFUL_HAIR", "WEARS_BRIGHT", "FROM_OP", "IS_PIRATE"));
            characters.add(createChar("zoro", R.drawable.zoro, "IS_MALE", "COLORFUL_HAIR", "HAS_SWORD", "WEARS_DARK", "FROM_OP", "IS_PIRATE"));
            characters.add(createChar("chopper", R.drawable.chopar, "IS_MALE", "NON_HUMAN_SKIN", "HAS_HAT", "POINTY_EARS", "FROM_OP", "IS_PIRATE", "CAN_TRANSFORM"));
            // Naruto
            characters.add(createChar("naruto", R.drawable.naruto, "IS_MALE", "HAIR_LIGHT", "SPIKY_HAIR", "FACE_MARKS", "WEARS_BRIGHT", "FROM_NARUTO", "PROTAGONIST", "IS_NINJA", "CAN_TRANSFORM"));
            characters.add(createChar("sasuke", R.drawable.sasuke, "IS_MALE", "HAIR_BLACK", "SPIKY_HAIR", "HAS_SWORD", "WEARS_DARK", "FROM_NARUTO", "IS_NINJA", "EYE_POWERS"));
            characters.add(createChar("sakura", R.drawable.sakura, "IS_FEMALE", "COLORFUL_HAIR", "WEARS_BRIGHT", "FROM_NARUTO", "IS_NINJA"));
            characters.add(createChar("tobi", R.drawable.tobi, "IS_MALE", "HAS_MASK", "WEARS_DARK", "FROM_NARUTO", "IS_VILLAIN", "IS_NINJA", "EYE_POWERS"));
            // Attack on Titan
            characters.add(createChar("eren", R.drawable.eren, "IS_MALE", "HAIR_BLACK", "HAS_CLOAK", "FROM_AOT", "PROTAGONIST", "CAN_TRANSFORM"));
            characters.add(createChar("mikasa", R.drawable.mikasa, "IS_FEMALE", "HAIR_BLACK", "HAS_CLOAK", "WEARS_DARK", "FROM_AOT"));
            characters.add(createChar("armor titan", R.drawable.armor_titan, "IS_MALE", "NON_HUMAN_SKIN", "HAIR_LIGHT", "FROM_AOT", "IS_VILLAIN", "CAN_TRANSFORM"));
            characters.add(createChar("colossal titan", R.drawable.colossal_titan, "IS_MALE", "NON_HUMAN_SKIN", "IS_BALD", "FROM_AOT", "IS_VILLAIN", "CAN_TRANSFORM"));
            // JJK
            characters.add(createChar("gojo", R.drawable.gojo, "IS_MALE", "HAIR_LIGHT", "HAS_MASK", "WEARS_DARK", "FROM_JJK", "EYE_POWERS"));
            characters.add(createChar("sukuna", R.drawable.sukuna, "IS_MALE", "COLORFUL_HAIR", "FACE_MARKS", "FROM_JJK", "IS_VILLAIN", "HAS_MAGIC"));
            characters.add(createChar("jogo", R.drawable.jogo, "IS_MALE", "NON_HUMAN_SKIN", "IS_BALD", "FROM_JJK", "IS_VILLAIN", "HAS_MAGIC"));
            characters.add(createChar("megumi", R.drawable.megumi, "IS_MALE", "HAIR_BLACK", "SPIKY_HAIR", "WEARS_DARK", "FROM_JJK", "HAS_MAGIC"));
            // Bleach
            characters.add(createChar("ichigo", R.drawable.ichigo, "IS_MALE", "COLORFUL_HAIR", "SPIKY_HAIR", "HAS_SWORD", "WEARS_DARK", "FROM_BLEACH", "PROTAGONIST", "CAN_TRANSFORM"));
            characters.add(createChar("rukia", R.drawable.rukia, "IS_FEMALE", "HAIR_BLACK", "HAS_SWORD", "WEARS_DARK", "FROM_BLEACH"));
            characters.add(createChar("aizen", R.drawable.aizen, "IS_MALE", "HAIR_BLACK", "FROM_BLEACH", "IS_VILLAIN", "HAS_MAGIC"));
            characters.add(createChar("yoruichi", R.drawable.yoruichi, "IS_FEMALE", "COLORFUL_HAIR", "SKIN_DARK", "WEARS_BRIGHT", "FROM_BLEACH"));
        }

        else if ("מארוול".equals(category)) {

            characters.add(createChar("iron man", R.drawable.iron_man, "IS_MALE", "FULL_MASK", "SKIN_NORMAL", "THEME_RED", "IS_AVENGER", "CAN_FLY", "USES_TECH"));
            characters.add(createChar("black widow", R.drawable.black_widow, "IS_FEMALE", "SKIN_NORMAL", "HAIR_RED", "THEME_DARK", "IS_AVENGER", "USES_TECH"));
            characters.add(createChar("hawkeye", R.drawable.hokay, "IS_MALE", "SKIN_NORMAL", "HAIR_BLACK", "HAIR_SHORT", "HAS_BEARD", "THEME_DARK","HAS_MAGIC", "IS_AVENGER", "USES_TECH"));
            characters.add(createChar("thor", R.drawable.thor, "IS_MALE", "SKIN_NORMAL", "HAS_BEARD","SUPER_STRENGTH","USES_TECH","HAS_MAGIC", "HAIR_BLONDE", "HAIR_LONG", "IS_AVENGER", "IS_GOD", "CAN_FLY", "ROYALTY"));
            characters.add(createChar("cap america", R.drawable.captain_america, "IS_MALE", "HAS_HELMET","USES_TECH", "ROYALTY", "SKIN_NORMAL", "IS_AVENGER", "USES_TECH"));
            characters.add(createChar("hulk", R.drawable.hulk, "IS_MALE", "MONSTROUS_LOOK", "CAN_SHRINK", "SKIN_GREEN","HAS_MAGIC", "HAIR_BLACK", "HAIR_SHORT", "IS_AVENGER", "SUPER_STRENGTH"));
            characters.add(createChar("dr. doom", R.drawable.dr_doom, "IS_MALE", "SKIN_NORMAL", "CAN_FLY", "FULL_MASK", "IS_VILLAIN", "HAS_MAGIC", "USES_TECH"));
            characters.add(createChar("spider-man", R.drawable.spider_man, "IS_MALE", "FULL_MASK", "SKIN_NORMAL","HAS_MAGIC","SUPER_STRENGTH", "THEME_RED", "IS_AVENGER"));
            characters.add(createChar("dr strange", R.drawable.doctor_strange, "IS_MALE", "SKIN_NORMAL", "HAS_BEARD", "HAIR_BLACK", "CAN_FLY", "HAS_MAGIC"));
            characters.add(createChar("scarlet", R.drawable.scarlet, "IS_FEMALE", "SKIN_NORMAL","CAN_FLY","HAS_HELMET_DECOR", "HAIR_RED", "HAIR_LONG", "THEME_RED", "HAS_MAGIC"));
            characters.add(createChar("ant-man", R.drawable.ant_man, "IS_MALE", "FULL_MASK", "SKIN_NORMAL", "IS_AVENGER", "USES_TECH", "CAN_SHRINK"));
            characters.add(createChar("loki", R.drawable.loki, "IS_MALE", "HAS_HELMET", "HAS_HELMET_DECOR","ROYALTY", "USES_TECH", "SKIN_NORMAL", "HAIR_BLACK", "IS_GOD", "IS_VILLAIN", "HAS_MAGIC"));
            characters.add(createChar("venom", R.drawable.venom, "IS_MALE", "MONSTROUS_LOOK", "SKIN_NORMAL","SUPER_STRENGTH", "CAN_SHRINK", "THEME_DARK","HAS_MAGIC", "IS_VILLAIN", "REGENERATION"));
            characters.add(createChar("cap marvel", R.drawable.captain_marvel, "IS_FEMALE", "SKIN_NORMAL","SUPER_STRENGTH","ROYALTY","HAS_MAGIC", "HAIR_BLONDE", "CAN_FLY"));
            characters.add(createChar("groot", R.drawable.groot, "IS_MALE", "MONSTROUS_LOOK","HAS_MAGIC", "CAN_SHRINK", "IS_GUARDIAN"));
            characters.add(createChar("wolverine", R.drawable.woolverin, "IS_MALE", "HAS_HELMET", "HAS_BEARD","HAS_MAGIC","HAS_HELMET_DECOR", "USES_TECH", "SKIN_NORMAL", "HAS_CLAWS", "REGENERATION"));
            characters.add(createChar("black panther", R.drawable.black_panter, "IS_MALE", "SKIN_NORMAL", "HAS_BEARD","USES_TECH", "HAIR_BLACK", "HAIR_SHORT", "THEME_DARK", "IS_AVENGER", "HAS_CLAWS", "ROYALTY"));
            characters.add(createChar("deadpool", R.drawable.deadpool, "IS_MALE", "FULL_MASK", "SKIN_NORMAL","USES_TECH", "THEME_RED", "REGENERATION"));
            characters.add(createChar("hela", R.drawable.hella, "IS_FEMALE", "HAS_HELMET", "HAS_HELMET_DECOR","ROYALTY", "CAN_FLY", "SKIN_NORMAL", "HAIR_BLACK", "THEME_DARK", "IS_GOD", "IS_VILLAIN", "HAS_MAGIC"));
            characters.add(createChar("star-lord", R.drawable.star_lord, "IS_MALE", "SKIN_NORMAL", "HAS_HELMET", "CAN_FLY","THEME_RED", "IS_GUARDIAN", "USES_TECH"));
            characters.add(createChar("gamora", R.drawable.gamora, "IS_FEMALE", "SKIN_GREEN", "HAIR_BLACK","MONSTROUS_LOOK", "HAIR_LONG", "IS_GUARDIAN"));
            characters.add(createChar("thanos", R.drawable.thanos, "IS_MALE", "MONSTROUS_LOOK", "SKIN_PURPLE","USES_TECH", "IS_VILLAIN", "SUPER_STRENGTH"));
            characters.add(createChar("mantis", R.drawable.mantis, "IS_FEMALE", "HAS_HELMET_DECOR","HAS_MAGIC", "SKIN_NORMAL", "HAIR_BLACK", "HAIR_LONG", "IS_GUARDIAN"));
            characters.add(createChar("vision", R.drawable.vision, "IS_MALE", "SKIN_PURPLE","HAS_MAGIC","USES_TECH", "IS_AVENGER","MONSTROUS_LOOK", "CAN_FLY", "ANDROID"));
        }
        return characters;
    }

    private static GameCharacter createChar(String name, int img, String... extraAttrs) {
        // יצירת הדמות עם שם באותיות קטנות בלבד
        GameCharacter c = new GameCharacter(name, img);
        // לולאה שעוברת על כל התכונות ששלחנו ומוסיפה אותן
        for (String attr : extraAttrs) {
            c.addAttr(attr, true);
        }
        return c;
    }

    public static HashMap<String, List<Question>> getQuestions(String category) {
        HashMap<String, List<Question>> map = new HashMap<>();

        if ("רגיל".equals(category)) {
            List<Question> regular = new ArrayList<>();

            regular.add(new Question("האם זה זכר?", "IS_MALE"));
            regular.add(new Question("האם זו בת?", "IS_FEMALE"));
            regular.add(new Question("האם לדמות יש עור כהה?", "SKIN_DARK"));

            regular.add(new Question("האם לדמות יש משקפיים?", "HAS_GLASSES"));
            regular.add(new Question("האם לדמות יש זקן?", "HAS_BEARD"));
            regular.add(new Question("האם לדמות יש שפם?", "HAS_MUSTACHE"));
            regular.add(new Question("האם יש לדמות סימן מיוחד על המצח (בינדי)?", "HAS_BINDI"));

            regular.add(new Question("האם הדמות חובשת כובע (רגיל או קש)?", "HAS_HAT"));
            regular.add(new Question("האם הדמות חובשת כיסוי ראש דתי או מסורתי (חיג'אב, טורבן או כיפה)?", "HAS_RELIGIOUS_HEADWEAR"));
            regular.add(new Question("האם יש לדמות עגילים?", "HAS_EARRINGS"));
            regular.add(new Question("האם יש לדמות עגילי חישוק?", "HAS_HOOP_EARRINGS"));
            regular.add(new Question("האם הדמות עונדת שרשרת?", "HAS_NECKLACE"));

            regular.add(new Question("האם השיער ממש מתולתל או ראסטות?", "HAIR_CURLY"));
            regular.add(new Question("האם לדמות יש צמות?", "HAIR_BRAIDS"));
            regular.add(new Question("האם צבע השיער בהיר (בלונדיני, ג'ינג'י או לבן)?", "HAIR_LIGHT"));
            regular.add(new Question("האם לדמות יש שיער שחור או חום?", "HAIR_BLACK"));
            regular.add(new Question("האם הדמות קירחת או כמעט קירחת?", "IS_BALD"));

            regular.add(new Question("האם הדמות לובשת חולצה עם צווארון?", "HAS_COLLAR"));
            regular.add(new Question("האם הדמות לובשת בגד כחול?", "WEARS_BLUE"));
            regular.add(new Question("האם הדמות לובשת בגד ירוק?", "WEARS_GREEN"));
            regular.add(new Question("האם הדמות לובשת בגד כתום או חום?", "WEARS_ORANGE"));
            regular.add(new Question("האם הדמות לובשת בגד אדום או ורוד?", "WEARS_RED"));
            regular.add(new Question("האם הדמות לובשת בגד סגול?", "WEARS_PURPLE"));
            regular.add(new Question("האם הדמות לובשת בגד צהוב?", "WEARS_YELLOW"));
            map.put("שאלות רגיל", regular);
        }


        if ("אנימה".equals(category)) {
            List<Question> animeVisual = new ArrayList<>();

            animeVisual.add(new Question("האם זה בן?", "IS_MALE"));
            animeVisual.add(new Question("האם זו בת?", "IS_FEMALE"));

            animeVisual.add(new Question("האם לדמות יש עור לא אנושי (ירוק/אפור/אדום)?", "NON_HUMAN_SKIN")); // פיקולו, ג'וגו, טיטאנים
            animeVisual.add(new Question("האם לדמות יש עור כהה?", "SKIN_DARK")); // יורואיצ'י
            animeVisual.add(new Question("האם הדמות חובשת מסכה או כיסוי עיניים?", "HAS_MASK")); // גוג'ו, טובי
            animeVisual.add(new Question("האם לדמות יש סימנים או קעקועים בולטים על הפנים?", "FACE_MARKS")); // סוקונה, נארוטו

            animeVisual.add(new Question("האם לדמות יש שיער שחור?", "HAIR_BLACK"));
            animeVisual.add(new Question("האם לדמות יש שיער צבעוני (כחול/ורוד/ירוק/כתום)?", "COLORFUL_HAIR"));
            animeVisual.add(new Question("האם לדמות יש שיער בהיר (בלונדיני/לבן)?", "HAIR_LIGHT"));
            animeVisual.add(new Question("האם השיער של הדמות קוצני מאוד?", "SPIKY_HAIR"));
            animeVisual.add(new Question("האם הדמות קירחת?", "IS_BALD"));

            animeVisual.add(new Question("האם הדמות לובשת בגד כתום או אדום?", "WEARS_BRIGHT"));
            animeVisual.add(new Question("האם הדמות לובשת בגד שחור או כהה מאוד?", "WEARS_DARK"));
            animeVisual.add(new Question("האם הדמות מחזיקה חרב או נשק קר?", "HAS_SWORD"));
            animeVisual.add(new Question("האם הדמות חובשת כובע?", "HAS_HAT"));
            animeVisual.add(new Question("האם הדמות לובשת צעיף או גלימה?", "HAS_CLOAK"));
            map.put("שאלות מראה (אנימה)", animeVisual);

            List<Question> anime = new ArrayList<>();
            anime.add(new Question("האם הדמות מדרגון בול?", "FROM_DBZ"));
            anime.add(new Question("האם הדמות מוואן פיס?", "FROM_OP"));
            anime.add(new Question("האם הדמות מנארוטו?", "FROM_NARUTO"));
            anime.add(new Question("האם הדמות ממתקפת הטיטאנים?", "FROM_AOT"));
            anime.add(new Question("האם הדמות מג'וג'וטסו קאיזן?", "FROM_JJK"));
            anime.add(new Question("האם הדמות מבליץ'?", "FROM_BLEACH"));
            anime.add(new Question("האם הדמות היא הגיבור הראשי?", "PROTAGONIST"));
            anime.add(new Question("האם הדמות היא נבל?", "IS_VILLAIN"));
            anime.add(new Question("האם הדמות יכולה לעוף?", "CAN_FLY"));
            anime.add(new Question("האם הדמות היא נינג'ה?", "IS_NINJA"));
            anime.add(new Question("האם הדמות היא פיראט?", "IS_PIRATE"));
            anime.add(new Question("האם הדמות יכולה לשנות צורה?", "CAN_TRANSFORM"));
            anime.add(new Question("האם הדמות משתמשת בקסם/אנרגיה?", "HAS_MAGIC"));
            anime.add(new Question("האם לדמות יש כוח מיוחד בעיניים?", "EYE_POWERS"));
            anime.add(new Question("האם הדמות היא סאיינית?", "IS_SAIYAN"));
            map.put("שאלות אנימה", anime);
        }

        if ("מארוול".equals(category)) {
            List<Question> marvelVisual = new ArrayList<>();
            // מגדר ומראה בסיסי
            marvelVisual.add(new Question("האם זה בן?", "IS_MALE"));
            marvelVisual.add(new Question("האם זו בת?", "IS_FEMALE"));
            marvelVisual.add(new Question("האם הדמות נראית לא אנושית?", "MONSTROUS_LOOK"));
            // כיסוי פנים (חשוב להבדלה בין דדפול, ספיידרמן, איירון מן)
            marvelVisual.add(new Question("האם הפנים של הדמות מכוסות לגמרי במסכה?", "FULL_MASK"));
            marvelVisual.add(new Question("האם לדמות יש מסכה חלקית?", "HAS_HELMET"));
            marvelVisual.add(new Question("האם יש או קרניים או כתר בתמונה?", "HAS_HELMET_DECOR"));
            // צבעי עור
            marvelVisual.add(new Question("האם הדמות ירוקה?", "SKIN_GREEN"));
            marvelVisual.add(new Question("האם הדמות אדומה/סגולה/ורודה?", "SKIN_PURPLE"));
            marvelVisual.add(new Question("האם לדמות צבע עור רגיל?", "SKIN_NORMAL"));
            // שיער ופנים
            marvelVisual.add(new Question("האם לדמות יש זקן או שפם שנראה בתמונה?", "HAS_BEARD"));
            marvelVisual.add(new Question("האם לדמות יש שיער בלונדיני?", "HAIR_BLONDE"));
            marvelVisual.add(new Question("האם לדמות יש שיער שחור?", "HAIR_BLACK"));
            marvelVisual.add(new Question("האם לדמות שיער ג'ינג'י?", "HAIR_RED"));
            marvelVisual.add(new Question("האם לדמות שיער ארוך שנראה בתמונה?", "HAIR_LONG"));
            // צבעי לבוש (חשוב להבדלה מהירה)
            marvelVisual.add(new Question("האם התלבושת היא בעיקר אדומה?", "THEME_RED"));
            marvelVisual.add(new Question("האם התלבושת היא שחורה?", "THEME_DARK"));
            map.put("שאלות מראה (מארוול)", marvelVisual);

            List<Question> marvel = new ArrayList<>();
            // שיוך קבוצתי
            marvel.add(new Question("האם הדמות היא מהנוקמים (Avengers)?", "IS_AVENGER"));
            marvel.add(new Question("האם הדמות משומרי הגלקסיה?", "IS_GUARDIAN"));
            // סוג דמות
            marvel.add(new Question("האם הדמות היא אל (God)?", "IS_GOD"));
            marvel.add(new Question("האם הדמות היא נבל (Villain)?", "IS_VILLAIN")); // תאנוס, הלה, דוקטור דום, ונום
            // כוחות ויכולות (הוספתי דברים קריטיים)
            marvel.add(new Question("האם הדמות יכולה לעוף?", "CAN_FLY"));
            marvel.add(new Question("האם לדמות יש כוח על טבעי?", "HAS_MAGIC"));
            marvel.add(new Question("האם הדמות משתמשת בטכנולוגיה או נשק?", "USES_TECH")); // סטאר לורד, הוקאיי
            marvel.add(new Question("האם לדמות יש טפרים (Claws)?", "HAS_CLAWS")); // וולברין, פנתר שחור
            marvel.add(new Question("האם הדמות מנהיגה או מלוכה?", "ROYALTY")); // פנתר שחור, תור
            marvel.add(new Question("האם הדמות יכולה להשתנות בגודל?", "CAN_SHRINK")); // אנטמן
            marvel.add(new Question("האם הדמות היא אנדרואיד?", "ANDROID")); // ויז'ן
            marvel.add(new Question("האם הדמות ידועה ביכולת ריפוי מהירה?", "REGENERATION")); // דדפול, וולברין
            marvel.add(new Question("האם לדמות יש כוח פיזי עצום (Super Strength)?", "SUPER_STRENGTH")); // האלק
            map.put("שאלות מארוול", marvel);
        }
        return map;
    }
}