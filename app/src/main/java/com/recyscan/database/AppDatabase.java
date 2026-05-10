package com.recyscan.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.recyscan.models.Product;
import com.recyscan.models.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database for RecyScan.
 * Contains tables: products, reminders.
 * Pre-populated with sample German recycling products.
 */
@Database(entities = {Product.class, Reminder.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    // ========== DAOs ==========
    public abstract ProductDao productDao();
    public abstract ReminderDao reminderDao();

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Thread pool for database write operations
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    /**
     * Get singleton database instance.
     * Creates database with pre-populated sample data on first launch.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "recyscan_database"
                            )
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback to populate database with sample data on first creation.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Populate with sample German recycling products
            databaseWriteExecutor.execute(() -> {
                ProductDao dao = INSTANCE.productDao();
                dao.insertAll(getSampleProducts());
            });
        }
    };

    /**
     * Returns a list of sample German products with recycling information.
     * Covers all main recycling categories in Germany.
     */
    private static List<Product> getSampleProducts() {
        List<Product> products = new ArrayList<>();

        // ==================== GELBER SACK (Yellow Bag) ====================
        // Plastics, metals, composite materials

        products.add(new Product(
                "Mineralwasser 0,5L PET",
                "4000950100010",
                "Gelber Sack",
                true, true, 0.25,
                "PET-Flasche im Pfandautomaten zurückgeben. Ohne Pfand: leer und zusammendrücken, in den Gelben Sack.",
                "rPET",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Cola 1,0L PET",
                "5449000000996",
                "Gelber Sack",
                true, true, 0.25,
                "Einweg-PET-Flasche mit Pfand. Am Pfandautomaten zurückgeben. Etikett kann dran bleiben.",
                "PET",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Joghurtbecher",
                "4014400900019",
                "Gelber Sack",
                true, false, 0.0,
                "Becher auslöffeln (nicht ausspülen nötig), Aludeckel abtrennen. Beides in den Gelben Sack. Nicht stapeln!",
                "PP",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Konservendose Mais",
                "4002809100123",
                "Gelber Sack",
                true, false, 0.0,
                "Dose ausspülen, Deckel nach innen drücken. In den Gelben Sack werfen.",
                "Weißblech",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Chips-Tüte",
                "4017100301001",
                "Gelber Sack",
                true, false, 0.0,
                "Tüte leer machen und zusammenfalten. In den Gelben Sack. Verbundmaterial wird in der Sortieranlage getrennt.",
                "Verbundstoff",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Milchkarton (Tetra Pak)",
                "4007500118002",
                "Gelber Sack",
                true, false, 0.0,
                "Karton leer machen, zusammenfalten und in den Gelben Sack. Deckel kann dran bleiben.",
                "Verbundkarton",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Shampoo-Flasche",
                "4015000961233",
                "Gelber Sack",
                true, false, 0.0,
                "Flasche möglichst leer machen. Deckel kann drauf bleiben. In den Gelben Sack.",
                "HDPE",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Alufolie",
                "0000000000000",
                "Gelber Sack",
                true, false, 0.0,
                "Zusammenknüllen und in den Gelben Sack. Muss nicht gereinigt werden, grobe Reste entfernen.",
                "Aluminium",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Sprudel 1,5L PET",
                "4100060004560",
                "Gelber Sack",
                true, true, 0.25,
                "Einweg-PET-Flasche: Pfandautomat. Deckel kann drauf bleiben. Etikett muss lesbar sein.",
                "PET",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Sprühsahne-Dose",
                "4002809100456",
                "Gelber Sack",
                true, false, 0.0,
                "Dose komplett entleeren. In den Gelben Sack. Nicht durchstechen oder ins Feuer werfen!",
                "Weißblech",
                "ic_gelber_sack"
        ));

        // ==================== ALTPAPIER (Paper) ====================

        products.add(new Product(
                "Zeitung / Tageszeitung",
                "",
                "Altpapier",
                true, false, 0.0,
                "Zeitungen gebündelt oder lose in die Papiertonne. Keine nassen oder verschmutzten Zeitungen.",
                "Papier",
                "ic_altpapier"
        ));

        products.add(new Product(
                "Karton / Versandkarton",
                "",
                "Altpapier",
                true, false, 0.0,
                "Kartons flach zusammenfalten und in die Papiertonne. Klebeband muss nicht entfernt werden.",
                "Wellpappe",
                "ic_altpapier"
        ));

        products.add(new Product(
                "Druckerpapier",
                "",
                "Altpapier",
                true, false, 0.0,
                "In die Papiertonne. Büroklammern und Heftklammern stören nicht bei der Sortierung.",
                "Papier",
                "ic_altpapier"
        ));

        products.add(new Product(
                "Eierkarton",
                "4337256100012",
                "Altpapier",
                true, false, 0.0,
                "Saubere Eierkartons in die Papiertonne. Verschmutzte in den Biomüll.",
                "Pappe",
                "ic_altpapier"
        ));

        products.add(new Product(
                "Pizzakarton",
                "",
                "Restmüll",
                false, false, 0.0,
                "Stark verschmutzte Pizzakartons gehören in den Restmüll! Saubere Teile abtrennen → Altpapier.",
                "Pappe",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Briefumschlag",
                "",
                "Altpapier",
                true, false, 0.0,
                "Briefumschläge mit Sichtfenster können trotzdem ins Altpapier – das Plastik wird herausgefiltert.",
                "Papier",
                "ic_altpapier"
        ));

        // ==================== ALTGLAS (Glass) ====================

        products.add(new Product(
                "Weinflasche (Grünglas)",
                "",
                "Altglas",
                true, false, 0.0,
                "Deckel/Korken entfernen. In den Grünglas-Container. Etiketten können dran bleiben.",
                "Glas",
                "ic_altglas"
        ));

        products.add(new Product(
                "Marmeladenglas",
                "",
                "Altglas",
                true, false, 0.0,
                "Deckel entfernen (→ Gelber Sack). Glas auslöffeln und in den Weißglas-Container.",
                "Glas",
                "ic_altglas"
        ));

        products.add(new Product(
                "Bierflasche (Mehrweg)",
                "4000950110019",
                "Altglas",
                true, true, 0.08,
                "Mehrweg-Bierflasche: Zurück in den Getränkemarkt! Pfand: 0,08€. Nicht in den Glascontainer!",
                "Glas",
                "ic_altglas"
        ));

        products.add(new Product(
                "Senfglas",
                "4003829400127",
                "Altglas",
                true, false, 0.0,
                "Deckel entfernen (→ Gelber Sack). Glas in den Weißglas-Container. Muss nicht gespült werden.",
                "Glas",
                "ic_altglas"
        ));

        products.add(new Product(
                "Olivenöl-Flasche",
                "",
                "Altglas",
                true, false, 0.0,
                "Grünglas-Container. Deckel entfernen. Flasche muss nicht ausgespült werden.",
                "Glas",
                "ic_altglas"
        ));

        // ==================== BIOMÜLL (Organic Waste) ====================

        products.add(new Product(
                "Bananenschale",
                "",
                "Biomüll",
                true, false, 0.0,
                "In die Biotonne. Obst- und Gemüsereste sind idealer Biomüll. Können auch kompostiert werden.",
                "Organisch",
                "ic_biomuell"
        ));

        products.add(new Product(
                "Kaffeesatz mit Filter",
                "",
                "Biomüll",
                true, false, 0.0,
                "Kaffeefilter samt Kaffeesatz in die Biotonne. Auch Teebeutel (ohne Metallklammer) sind erlaubt.",
                "Organisch",
                "ic_biomuell"
        ));

        products.add(new Product(
                "Eierschalen",
                "",
                "Biomüll",
                true, false, 0.0,
                "Eierschalen in die Biotonne. Zerkleinert verrotten sie schneller.",
                "Organisch",
                "ic_biomuell"
        ));

        products.add(new Product(
                "Küchenrolle (benutzt)",
                "",
                "Biomüll",
                true, false, 0.0,
                "Benutzte Küchentücher (ohne Chemikalien) in die Biotonne. Unbenutzte → Altpapier.",
                "Papier/Organisch",
                "ic_biomuell"
        ));

        products.add(new Product(
                "Laub / Gartenabfälle",
                "",
                "Biomüll",
                true, false, 0.0,
                "Laub, Rasenschnitt und kleine Äste in die Biotonne. Große Mengen zum Wertstoffhof.",
                "Organisch",
                "ic_biomuell"
        ));

        products.add(new Product(
                "Brotreste / altes Brot",
                "",
                "Biomüll",
                true, false, 0.0,
                "Altes Brot und Brotreste in die Biotonne. Verpackung vorher entfernen!",
                "Organisch",
                "ic_biomuell"
        ));

        // ==================== RESTMÜLL (Residual Waste) ====================

        products.add(new Product(
                "Zigarettenstummel",
                "",
                "Restmüll",
                false, false, 0.0,
                "Zigarettenreste immer in den Restmüll. Niemals in Biomüll oder auf den Boden werfen!",
                "Sondermüll",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Windeln",
                "",
                "Restmüll",
                false, false, 0.0,
                "Benutzte Windeln gehören in den Restmüll. In einer Tüte verschließen gegen Geruch.",
                "Verbundstoff",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Staubsaugerbeutel",
                "",
                "Restmüll",
                false, false, 0.0,
                "Volle Staubsaugerbeutel in den Restmüll. Beutel gut verschließen.",
                "Verbundstoff",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Kaputte Tasse / Keramik",
                "",
                "Restmüll",
                false, false, 0.0,
                "Keramik und Porzellan gehören NICHT ins Altglas! Immer in den Restmüll. Scherben einwickeln.",
                "Keramik",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Zahnbürste",
                "",
                "Restmüll",
                false, false, 0.0,
                "Alte Zahnbürsten in den Restmüll. Nicht in den Gelben Sack (zu klein für Sortieranlage).",
                "Kunststoff",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Kugelschreiber",
                "",
                "Restmüll",
                false, false, 0.0,
                "Kugelschreiber und Stifte in den Restmüll. Verbundmaterial ist nicht recycelbar.",
                "Verbundstoff",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Foto / Fotopapier",
                "",
                "Restmüll",
                false, false, 0.0,
                "Fotos und Fotopapier sind beschichtet und gehören in den Restmüll, NICHT ins Altpapier!",
                "Beschichtetes Papier",
                "ic_restmuell"
        ));

        // ==================== PFAND Items ====================

        products.add(new Product(
                "Energy Drink Dose 0,25L",
                "9002490100070",
                "Gelber Sack",
                true, true, 0.25,
                "Einwegdose mit Pfand. Am Pfandautomaten zurückgeben. Dose nicht zusammendrücken!",
                "Aluminium",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Bierflasche (Einweg)",
                "4100060034567",
                "Gelber Sack",
                true, true, 0.25,
                "Einweg-Glasflasche mit 0,25€ Pfand. Am Pfandautomaten zurückgeben.",
                "Glas",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Wasser Mehrweg-Glasflasche",
                "4000950200012",
                "Altglas",
                true, true, 0.15,
                "Mehrwegflasche: Zurück in den Getränkemarkt! Pfand: 0,15€. Wiederbefüllung schont die Umwelt.",
                "Glas",
                "ic_altglas"
        ));

        // ==================== Additional Common Items ====================

        products.add(new Product(
                "Plastiktüte / Tragetasche",
                "",
                "Gelber Sack",
                true, false, 0.0,
                "Leere Plastiktüten in den Gelben Sack. Besser: Stoffbeutel verwenden!",
                "LDPE",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Backpapier",
                "",
                "Restmüll",
                false, false, 0.0,
                "Backpapier ist beschichtet und gehört in den Restmüll, nicht ins Altpapier!",
                "Beschichtetes Papier",
                "ic_restmuell"
        ));

        products.add(new Product(
                "Glasreiniger-Flasche",
                "4015000311250",
                "Gelber Sack",
                true, false, 0.0,
                "Leere Reinigungsmittelflaschen in den Gelben Sack. Sprühkopf kann dran bleiben.",
                "PET",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Styropor-Verpackung",
                "",
                "Gelber Sack",
                true, false, 0.0,
                "Verpackungs-Styropor in den Gelben Sack. Große Mengen zum Wertstoffhof.",
                "EPS",
                "ic_gelber_sack"
        ));

        products.add(new Product(
                "Batterien",
                "",
                "Sondermüll",
                false, false, 0.0,
                "Batterien NIEMALS in den Hausmüll! Kostenlose Rückgabe in Supermärkten und Drogerien.",
                "Schwermetalle",
                "ic_restmuell"
        ));

        return products;
    }
}
