package br.com.rmtcgames.rmtcgamesserver.resource;

import br.com.rmtcgames.rmtcgamesserver.domain.Player;
import com.google.cloud.firestore.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/position")
public class PositionResource {

    private final Firestore firestore;
    private final CollectionReference collectionReferenceJogadores, collectionReferenceGrupoJogadores;

    private final List<Player> playersLocalCache = new ArrayList<>();

//    private final EventListener jogadorEventLister = (EventListener<DocumentSnapshot>) (documentSnapshot, error) -> {
//
//        if(error!=null) {
//            System.out.println("Jogador Listen failed: " + error);
//            return;
//        }
//
//        if(documentSnapshot != null && documentSnapshot.exists()){
//            System.out.println("Executando lister apenas do documento "+documentSnapshot.getData());
//
//            Player currentPlayer = convertDocumentToPlayer(documentSnapshot);
//            addOrUpdatePlayer(currentPlayer);
//
//            System.out.println("Numero de total de jogadores "+ playersLocalCache.size());
//
//            List<Player> otherPlayers = new ArrayList<>(playersLocalCache);
//
//            System.out.println("antes de remover");
//            otherPlayers.forEach(System.out::println);
//
//            otherPlayers.remove(currentPlayer);
//
//            System.out.println("depois de remover");
//            otherPlayers.forEach(System.out::println);
//        }
//        else {
//            System.out.println("Current data: null");
//            assert documentSnapshot != null;
//            Player playerToRemove = new Player(documentSnapshot.getId());
//            playersLocalCache.remove(playerToRemove);
//        }
//    };

    Player convertDocumentToPlayer(DocumentSnapshot documentSnapshot) {
        Player player = documentSnapshot.toObject(Player.class);
        assert player != null;
        player.setId(documentSnapshot.getId());
        return player;
    }

    void addOrUpdatePlayer(Player player){
        if(!playersLocalCache.contains(player))
            playersLocalCache.add(player);
        else{
            int index = playersLocalCache.indexOf(player);
            playersLocalCache.set(index, player);
        }
    }

    void calcularProximos(Player currentPlayer){
//        System.out.println("Executando lister apenas do documento "+currentPlayer.toString());

//        System.out.println("Numero de total de jogadores "+ playersLocalCache.size());

        List<Player> otherPlayers = new ArrayList<>(playersLocalCache);

//        System.out.println("antes de remover");
//        otherPlayers.forEach(System.out::println);

        otherPlayers.remove(currentPlayer);

//        System.out.println("depois de remover");
//        otherPlayers.forEach(System.out::println);

        System.out.println("Current Player: "+currentPlayer.toString());

        for (Player other : otherPlayers) {
            System.out.println("Other Player: "+other.toString());
            boolean isNear = isNearToPoint(currentPlayer.getPosicao().getLatitude(),
                            currentPlayer.getPosicao().getLongitude(), other.getPosicao().getLatitude(),
                            other.getPosicao().getLongitude(), 8.0f);
            System.out.println(isNear);
        }
    }

    public PositionResource(Firestore firestore) {
        this.firestore = firestore;

        collectionReferenceJogadores = firestore.collection("jogadores");
        collectionReferenceGrupoJogadores = firestore.collection("grupoJogagores");

        collectionReferenceJogadores.addSnapshotListener((querySnapshots, error) -> {
            if (error != null)
                System.out.println("Error: " + error);

            if (querySnapshots != null && !querySnapshots.getDocumentChanges().isEmpty()) {
                for (DocumentChange documentChange : querySnapshots.getDocumentChanges()) {
                    switch (documentChange.getType()) {
                        case ADDED:
                            System.out.println("documento adicionado");
//                            String documentID = documentChange.getDocument().getId();
                            Player newPlayer = convertDocumentToPlayer(documentChange.getDocument());
//                            playersLocalCache.add(newPlayer);
                            addOrUpdatePlayer(newPlayer);
                            calcularProximos(newPlayer);
//                            collectionReferenceJogadores.document(documentChange.getDocument().getId())
//                                    .addSnapshotListener(jogadorEventLister);
                            break;
                        case MODIFIED:
                            System.out.println("documento modificado");
                            Player updatePlayer = convertDocumentToPlayer(documentChange.getDocument());
                            addOrUpdatePlayer(updatePlayer);
                            calcularProximos(updatePlayer);
                            break;
                        case REMOVED:
                            System.out.println("documento removido");
                            Player playerToRemove = new Player(documentChange.getDocument().getId());
                            playersLocalCache.remove(playerToRemove);
                            playersLocalCache.forEach(System.out::println);
                            break;
                    }
                }
            }
        });

//        ApiFuture<QuerySnapshot> documentsApiFuture = collectionReferenceJogadores.get();
//        try {
//            QuerySnapshot documentSnapshots = documentsApiFuture.get();
//            List<QueryDocumentSnapshot> documents = documentSnapshots.getDocuments();
//            for (QueryDocumentSnapshot document : documents) {
//                System.out.println(document.getData());
//            }
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }

//        collectionReferenceJogadores.addSnapshotListener((queryDocumentSnapshots, e) -> {
//
//            System.out.println("Executando Lister");
//            assert queryDocumentSnapshots != null;
//
//            List<QueryDocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
//            List<Player> playersLocalCache = new ArrayList<>();
//
//            for (QueryDocumentSnapshot document: documents) {
//                Player player = document.toObject(Player.class);
//                player.setId(document.getId());
//                playersLocalCache.add(player);
//            }
//
//            for (Player player : playersLocalCache){
//
//                System.out.println("player 1: "+player.toString());
//                List<String> proximos = new ArrayList<>();
//                List<Player> otherPlayers = new ArrayList<>(playersLocalCache);
//                otherPlayers.remove(player);
//
//                for (Player otherPlayer : otherPlayers) {
//                    System.out.println("player 2: "+ otherPlayer);
//                    boolean isNear = isNearToPoint(player.getPosicao().getLatitude(),
//                            player.getPosicao().getLongitude(), otherPlayer.getPosicao().getLatitude(),
//                            otherPlayer.getPosicao().getLongitude(), 8.0f);
//                    System.out.println(isNear);
//                    if(isNear)
//                        proximos.add(otherPlayer.getId());
//                }
//
//                HashMap<String, List<String>> ids = new HashMap<>();
//                ids.put("proximos", proximos);
//                collectionReferenceGrupoJogadores.document(player.getId()).set(ids);
//            }
//        });
    }

//    void deleteCollection(CollectionReference collection, int batchSize) {
//        try {
//            // retrieve a small batch of documents to avoid out-of-memory errors
//            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
//            int deleted = 0;
//            // future.get() blocks on document retrieval
//            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
//            for (QueryDocumentSnapshot document : documents) {
//                document.getReference().delete();
//                ++deleted;
//            }
//            if (deleted >= batchSize) {
//                // retrieve and delete another batch
//                deleteCollection(collection, batchSize);
//            }
//        } catch (Exception e) {
//            System.err.println("Error deleting collection : " + e.getMessage());
//        }
//    }

    public boolean isNearToPoint(double usrLat, double usrLng, double refLat, double refLng, float distance_km) {
        double ky = 40000 / 360;
        double kx = Math.cos(Math.PI * refLat / 180.0) * ky;
        double dx = Math.abs(refLng - usrLng) * kx;
        double dy = Math.abs(refLat - usrLat) * ky;
        double calc1 = Math.sqrt(dx * dx + dy * dy);
        System.out.println(calc1);
        return calc1 <= distance_km;

    }
}
