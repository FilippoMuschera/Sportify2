import com.sportify.joinmatch.JoinMatchBean;
import com.sportify.joinmatch.JoinMatchController;
import com.sportify.joinmatch.ResultSetEntity;
import com.sportify.user.UserEntity;
import com.sportify.user.UserPreferences;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestJoinMatchController {

    @Test
    void testFindJoinableMatch(){
        /* In questo test case vogliamo verificare che sia corretta la gestione dell'eccezione in cui non venga trovato nessuno sport center nel raggio scelto
         * dall'utente. In quel caso infatti il metodo findJoinableMatch() catturerà l'eccezione generata da getNearSportCenter, e proverà a ripetere la ricerca
         * con un raggio maggiorato.
         */
        JoinMatchBean bean = new JoinMatchBean();

        //Per prima cosa compiliamo la bean da passare al controller
        bean.setDistanceIsImportant(true);
        bean.setDistanceIsImportant(false);
        bean.setSelectedSport("Football");
        bean.setPreferredStartingTime("8");
        bean.setMaxResults("1");

        /*Adesso settiamo l'utente che sta "usando" lo use case. Siamo in ambiente di testing e quindi sappiamo che l'utente generato con i seguenti
        * parametri, non troverà uno sport center nel suo raggio scelto (3 km), ma sappiamo che gli verrà ritornato uno sport center dalla ricerca con
        * raggio maggiorato (25 km), di cui controlleremo la distanza */

        UserEntity user = UserEntity.getInstance();
        UserPreferences preferences = new UserPreferences(3, true, true, true, true, true, "Via Carlo Ilarione Petitti 23, Milano, 20149");
        user.setPreferences(preferences);

        //A questo punto facciamo partire lo use case, e poi leggiamo il risultato

        JoinMatchController controller = new JoinMatchController();
        controller.findJoinableMatch(bean);
        ResultSetEntity resultSet = bean.getResultSet();

        //Ora controlliamo che il risultato sia quello che ci aspettiamo (un solo sport center, a più di 3 km di distanza)
        System.out.println(resultSet.getElements().get(0).getNameSC() + " is " + resultSet.getElements().get(0).getDistance() +
                " km away");
        boolean isCorrect = resultSet.getElements().size() == 1 && !(resultSet.getElements().get(0).getDistance() < 3);
        assertTrue(isCorrect);
    }
}
