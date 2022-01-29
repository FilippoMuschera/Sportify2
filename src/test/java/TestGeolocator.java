import com.sportify.geolocation.Geolocator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/********************
 * Filippo Muscherà *
 ********************/

class TestGeolocator {

    String address = "Via del Politecnico 1, Roma, 00133";

    @Test
    void testGetLat(){
        //Vogliamo testare la correttezza dell'output prodotto dalla classe Geolocator nel metodo getLat()
        Geolocator g = Geolocator.getInstance();
        double correctLat = 41.85594;// 12.622605635099696
        double calculatedLat = g.getLat(address);
        System.out.println("Google Maps lat: " + correctLat + ", Sportify app calculated the following coordinate: " + calculatedLat);
        //Il delta non è zero perchè una minima imprecisione è accettabile per i fini dell'applicazione
        assertEquals(correctLat, calculatedLat, 0.001);

}
    @Test
    void testGetLng(){
        //Vogliamo testare la correttezza dell'output prodotto dalla classe Geolocator nel metodo getLng()
        Geolocator g = Geolocator.getInstance();
        double correctLng = 12.6205;
        double calculatedLng = g.getLng(address);
        System.out.println("Google Maps lng: " + correctLng + ", Sportify app calculated the following coordinate: " + calculatedLng);
        //Il delta non è zero perchè una minima imprecisione è accettabile per i fini dell'applicazione
        assertEquals(correctLng, calculatedLng, 0.001);
    }

}
