package br.com.rmtcgames.rmtcgamesserver.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value(value = "classpath:firebase-adminsdk.json")
    private Resource firebaseConfigFile;

    @Bean
    public FirebaseApp provideFirebaseOptions() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(firebaseConfigFile.getInputStream()))
                .setDatabaseUrl("firebase-adminsdk-0zziv@inbound-object-125823.iam.gserviceaccount.com")
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    @Qualifier("main")
    public Firestore provideFirestoreDatabaseReference() throws IOException {
        FirestoreOptions options = FirestoreOptions.newBuilder()
                .setTimestampsInSnapshotsEnabled(true)
                .setCredentials(GoogleCredentials.fromStream(firebaseConfigFile.getInputStream()))
                .build();
        return options.getService();
//        return FirestoreClient.getFirestore(firebaseApp);
    }

//    @Bean
//    @Qualifier("main")
//    public DatabaseReference provideFirebaseDatabaseReference(FirebaseApp firebaseApp) {
//        FirebaseDatabase.getInstance(firebaseApp).setPersistenceEnabled(false);
//        return FirebaseDatabase.getInstance(firebaseApp).getReference();
//    }
//
//    @Bean
//    public FirebaseAuth provideFirebaseOAuth(FirebaseApp firebaseApp) { return FirebaseAuth.getInstance(firebaseApp); }
}