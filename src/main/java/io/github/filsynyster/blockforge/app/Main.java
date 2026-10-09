package io.github.filsynyster.blockforge.app;

import io.github.filsynyster.blockforge.app.models.Contact;
import io.github.filsynyster.blockforge.app.repositories.ContactRepository;
import io.github.filsynyster.blockforge.database.core.PersistenceContext;
import io.github.filsynyster.blockforge.database.core.PersistenceContextFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Main /*extends Application*/ {

    /*@Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Agenda");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }*/

    public static void main(String[] args) {

        // launch(args);

        PersistenceContext context = PersistenceContextFactory.createOrmLiteContext("agenda-persistence");
        ContactRepository rep = context.getRepository(Contact.class);

        // =========================================================
        // 1. CONTACT
        // =========================================================

        System.out.println();
        System.out.println("========== Contact ==========");

        Contact contact = rep.findByName("Samuel Lino");

        System.out.println("findByName:");
        System.out.println(contact);


        // =========================================================
        // 2. CONTACT POR ID
        // =========================================================

        System.out.println();
        System.out.println("========== Contact por ID ==========");

        Contact contactById = rep.findByIdNative(1L);

        System.out.println("findByIdNative:");
        System.out.println(contactById);


        // =========================================================
        // 3. LIST
        // =========================================================

        System.out.println();
        System.out.println("========== List ==========");

        List<Contact> contacts = rep.findAllNative();

        System.out.println("findAllNative:");
        System.out.println(contacts);

        // =========================================================
        // 4. LIST COM WHERE
        // =========================================================

        System.out.println();
        System.out.println("========== List - idade ==========");

        List<Contact> contactsByAge = rep.findByAge();

        System.out.println("findByAge:");
        System.out.println(contactsByAge);


        // =========================================================
        // 5. LIST COM PARÂMETRO
        // =========================================================

        System.out.println();
        System.out.println("========== List - idade parametrizada ==========");

        List<Contact> contactsOlderThan = rep.findByAgeGreaterThan(10);

        System.out.println("findByAgeGreaterThan:");
        System.out.println(contactsOlderThan);


        // =========================================================
        // 6. SET
        // =========================================================

        System.out.println();
        System.out.println("========== Set ==========");

        Set<Contact> contactSet = rep.findAllAsSet();

        System.out.println("findAllAsSet:");
        System.out.println(contactSet);


        // =========================================================
        // 7. SET COM WHERE
        // =========================================================

        System.out.println();
        System.out.println("========== Set - idade ==========");

        Set<Contact> adultSet = rep.findAdultsAsSet();

        System.out.println("findAdultsAsSet:");
        System.out.println(adultSet);

        // =========================================================
        // 10. OPTIONAL POR NOME
        // =========================================================

        System.out.println();
        System.out.println("========== Optional - nome ==========");

        Optional<Contact> optionalByName = rep.findOptionalByName("João");

        System.out.println("findOptionalByName:");
        System.out.println(optionalByName);


        // =========================================================
        // 11. OPTIONAL POR ID
        // =========================================================

        System.out.println();
        System.out.println("========== Optional - ID ==========");

        Optional<Contact> optionalById = rep.findOptionalById(1L);

        System.out.println("findOptionalById:");
        System.out.println(optionalById);


        // =========================================================
        // 12. STREAM
        // =========================================================

        System.out.println();
        System.out.println("========== Stream ==========");

        try (Stream<Contact> stream = rep.findAllAsStream()) {

            stream.forEach(contactItem ->
                    System.out.println("Stream: " + contactItem)
            );
        }


        // =========================================================
        // 13. STREAM COM WHERE
        // =========================================================

        System.out.println();
        System.out.println("========== Stream - idade ==========");

        try (Stream<Contact> stream = rep.findByAgeAsStream()) {

            stream.forEach(contactItem ->
                    System.out.println("Stream idade: " + contactItem)
            );
        }


        // =========================================================
        // FECHA O CONTEXTO
        // =========================================================

        context.close();
    }
}