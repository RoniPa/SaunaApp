package fi.jamk.saunaapp.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import fi.jamk.saunaapp.BuildConfig;
import io.reactivex.Observable;

import fi.jamk.saunaapp.models.Rating;
import fi.jamk.saunaapp.models.Sauna;


/**
 * Service containing some general
 * actions regarding sauna ratings.
 */
public class RatingService {
    public static final String TAG = "RatingService";

    private FirebaseUser mUser;
    private FirebaseAuth authInstance;
    private FirebaseDatabase db;

    private HashMap<String, String> cachedRatingMap;

    private RatingService(FirebaseDatabase db) {
        this.authInstance = FirebaseAuth.getInstance();
        this.cachedRatingMap = new HashMap<>();
        this.db = db;
    }

    public static RatingService newInstance() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        return new RatingService(db);
    }

    /**
     * Save sauna rating, push rating to local map.
     *
     * ratings
     *     |__ id
     *          |_ rating data
     *
     * _hasRated
     *     |__ user id
     *            |__ sauna id : rating id
     *                - map of rated items (user can rate an item only once)
     *
     * @param rating    Rating object
     */
    public void saveRating(final Rating rating) {
        DatabaseReference mMetaReference = db.getReference("_hasRated").child(rating.getUser());
        DatabaseReference mReviewReference = db.getReference("ratings");

        rating.setId(mReviewReference.push().getKey());

        // Save knowledge that this user has rated this sauna already.
        // Stored as { userId { saunaId => ratingId } }
        mMetaReference.child(rating.getSaunaId()).setValue(rating.getId());
        mReviewReference.child(rating.getId()).setValue(rating);

        // Calculate & update rating for sauna
        DatabaseReference mSaunaReference = db.getReference("saunas/" + rating.getSaunaId());
        mSaunaReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Sauna s = mutableData.getValue(Sauna.class);
                if (s == null) {
                    return Transaction.success(mutableData);
                }

                s.setRatingCount(s.getRatingCount() + 1);
                double ratingDelta = rating.getRating() - s.getRating();
                s.setRating(s.getRating() + (ratingDelta / s.getRatingCount()));

                // Set value and report transaction success
                mutableData.setValue(s);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed, push to map
                cachedRatingMap.put(rating.getSaunaId(), rating.getId());
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    /**
     * Checks if current user has rated given sauna.
     *
     * @param saunaId
     * @return  true if has rated
     */
    public Observable<Boolean> hasRated(final String saunaId) {
        if (mUser == null) {
            this.mUser = authInstance.getCurrentUser();
        }

        if (!this.cachedRatingMap.containsKey(saunaId)) {
            return Observable.create(emitter ->
                db.getReference("_hasRated").child(mUser.getUid()).child(saunaId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                cachedRatingMap.put(saunaId, dataSnapshot.getValue(String.class));
                                emitter.onNext(true);
                            } else {
                                emitter.onNext(false);
                            }
                            emitter.onComplete();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // On errors disable rating
                            emitter.onNext(true);
                            emitter.onComplete();
                        }
                    })
            );
        } else {
            return Observable.create(emitter -> {
                if (cachedRatingMap.get(saunaId).isEmpty()) {
                    emitter.onNext(false);
                } else {
                    emitter.onNext(true);
                }

                emitter.onComplete();
            });
        }
    }

    public Observable<Rating> getRating(final String saunaId) {
        return Observable.create(emitter -> {
            final ValueEventListener l = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        emitter.onNext(dataSnapshot.getValue(Rating.class));
                    }
                    emitter.onComplete();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    emitter.onComplete();
                }
            };

            if (cachedRatingMap.containsKey(saunaId) && !cachedRatingMap.get(saunaId).isEmpty()) {
                db.getReference("ratings").child(cachedRatingMap.get(saunaId)).addListenerForSingleValueEvent(l);
            } else {
                hasRated(saunaId)
                    .subscribe(hasRated -> {
                        if (hasRated) {
                            // We assert that value has been cached from previous call.
                            if (BuildConfig.DEBUG && !cachedRatingMap.containsKey(saunaId)) {
                                throw new RuntimeException("Value has not been cached for recursive call.");
                            }
                            db.getReference("ratings").child(cachedRatingMap.get(saunaId)).addListenerForSingleValueEvent(l);
                        } else {
                            emitter.onComplete();
                        }
                    });
            }
        });
    }
}
