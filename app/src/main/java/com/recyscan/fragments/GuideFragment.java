package com.recyscan.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.recyscan.R;
import com.recyscan.adapters.GuideCategoryAdapter;
import com.recyscan.models.RecyclingCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Guide Fragment - Shows German recycling categories with descriptions.
 * Displays: Biomüll, Altpapier, Gelber Sack, Altglas, Restmüll, Sondermüll.
 */
public class GuideFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvGuide = view.findViewById(R.id.rv_guide_categories);
        rvGuide.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGuide.setAdapter(new GuideCategoryAdapter(getGuideCategories()));
    }

    /**
     * Create the list of recycling categories with full descriptions.
     */
    private List<RecyclingCategory> getGuideCategories() {
        List<RecyclingCategory> categories = new ArrayList<>();

        categories.add(new RecyclingCategory(
                "Biomüll",
                "Organische Abfälle aus Küche und Garten. Wird zu Kompost und Biogas verarbeitet.",
                "Obst- und Gemüsereste, Kaffeesatz, Teebeutel, Eierschalen, Gartenabfälle, Laub, Blumen, altes Brot",
                "Bananenschale, Apfelgehäuse, Kartoffelschalen, Rasenschnitt",
                R.drawable.ic_biomuell, R.color.brown_light, "#795548"
        ));

        categories.add(new RecyclingCategory(
                "Altpapier",
                "Papier, Pappe und Karton werden recycelt und zu neuem Papier verarbeitet.",
                "Zeitungen, Zeitschriften, Kartons, Briefe, Kataloge, Bücher, Schreibpapier, Eierkartons",
                "Tageszeitung, Amazon-Karton, Druckerpapier, Briefumschläge",
                R.drawable.ic_altpapier, R.color.blue_light, "#1565C0"
        ));

        categories.add(new RecyclingCategory(
                "Gelber Sack",
                "Verpackungen aus Kunststoff, Metall und Verbundmaterialien. Wird sortiert und recycelt.",
                "Plastikflaschen, Joghurtbecher, Konservendosen, Alufolie, Tetrapaks, Folien, Spraydosen, Styropor",
                "PET-Flasche, Chipstüte, Milchkarton, Konservendose",
                R.drawable.ic_gelber_sack, R.color.yellow_light, "#F9A825"
        ));

        categories.add(new RecyclingCategory(
                "Altglas",
                "Glas wird nach Farben getrennt gesammelt und kann endlos recycelt werden.",
                "Flaschen (Wein, Saft, Öl), Einmachgläser, Marmeladengläser, Parfümflakons",
                "Weinflasche, Gurkenglas, Olivenölflasche, Senfglas",
                R.drawable.ic_altglas, R.color.teal_light, "#00897B"
        ));

        categories.add(new RecyclingCategory(
                "Restmüll",
                "Alles, was nicht recycelbar ist und in keine andere Tonne gehört.",
                "Windeln, Zigaretten, Hygieneartikel, Keramik, verschmutztes Papier, Staubsaugerbeutel, Fotos",
                "Zahnbürste, Kugelschreiber, kaputte Tasse, Backpapier",
                R.drawable.ic_restmuell, R.color.grey_light, "#616161"
        ));

        categories.add(new RecyclingCategory(
                "Sondermüll",
                "Gefährliche Abfälle, die getrennt entsorgt werden müssen. Niemals in den Hausmüll!",
                "Batterien, Akkus, Farben, Lacke, Medikamente, Elektrogeräte, Energiesparlampen, Chemikalien",
                "Alte Batterien → Supermarkt, Elektroschrott → Wertstoffhof",
                R.drawable.ic_warning, R.color.red_light, "#D32F2F"
        ));

        return categories;
    }
}
