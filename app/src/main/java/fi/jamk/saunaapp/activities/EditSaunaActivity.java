package fi.jamk.saunaapp.activities;

import android.content.Intent;
import android.os.Bundle;

import fi.jamk.saunaapp.R;
import fi.jamk.saunaapp.models.Sauna;

public class EditSaunaActivity extends BaseActivity {
    private Sauna sauna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sauna);

        Intent intent = getIntent();
        sauna = intent.getParcelableExtra(DETAILS_SAUNA);

        if (sauna != null) {
            setTitle(R.string.title_activity_edit_sauna);
        } else {
            setTitle(R.string.title_activity_add_sauna);
            sauna = new Sauna();
        }
    }
}
