import com.sportify.geolocation.Geolocator;
import com.sportify.sportcenter.GetSportCenterDAO;
import com.sportify.user.UserEntity;
import com.sportify.user.UserPreferences;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/********************
 * Filippo Muscherà *
 ********************/

class TestGetSportCenterDAO {
    @Test
    void testGetNearSportCenter() {
        /* Vogliamo testare la correttezza dell'output prodotto dal metodo getNearSportCEnter() di AddSportCenterDAO,
         * che ha la funzionalità di restituire il nome dei centri sportivi nel raggio selezionato dall'utente nell
         * sue preferenze
         */
        UserEntity user = UserEntity.getInstance();
        UserPreferences preferences = new UserPreferences(5, true, true, true, true, true, "Piazza del Duomo 12, Milano, 20122");

        //Creiamo un'istanza di utente da poter utilizzare a scopo di test. L'utente ha le preferenze impostate di default al momento del signup.
        user.setPreferences(preferences);
        Geolocator g = Geolocator.getInstance();

        /*Siamo in ambiente di test, perciò è stato opportunamente inserito un solo sport center nel raggio dell'indirizzo dell'utente appena creato,
         * perciò siamo sicuri di quale debba essere il risultato.
         */

        GetSportCenterDAO dao = GetSportCenterDAO.getInstance();
        Map<String, Double> map = dao.getNearSportCenters("Football", 3, g.getLat(user.getPreferences().getUserAddress()),
                g.getLng(user.getPreferences().getUserAddress()));
        System.out.println(map.keySet().size() + " result found: " + map.keySet());
        boolean result = map.keySet().size() == 1 || map.containsKey("Test Sport Center Milano");
        assertTrue(result);


    }

}