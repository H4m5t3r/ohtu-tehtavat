package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KauppaTest {
    private Pankki pankki;
    private Viitegeneraattori viite;
    private Varasto varasto;
    
    @Before
    public void setUp() {
        pankki = mock(Pankki.class);
        viite = mock(Viitegeneraattori.class);
        varasto = mock(Varasto.class);
        // ...
    }

    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), anyInt());   
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    @Test
    public void tilisiirtoKutsutaanOikeallaAsiakkaallaTilinumeroillaJaSummalla() {
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(5));
    }
    
    @Test
    public void tilisiirtoToimiiOikeinKahdellaEriTuotteella() {
        when(viite.uusi()).thenReturn(42);
        
        // tuote 1
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        
        //tuote 2
        when(varasto.saldo(2)).thenReturn(3); 
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipä", 6));

        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // asiointi
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(11));
    }
    
    @Test
    public void tilisiirtoToimiiOikeinKahdellaSamallaTuotteella() {
        when(viite.uusi()).thenReturn(42);
        
        // tuote
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // asiointi
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(1);
        k.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(10));
    }
    
    @Test
    public void tilisiirtoToimiiOikeinKunVarastoOnTyhjä() {
        when(viite.uusi()).thenReturn(42);
        
        // tuote 1
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        
        // tuote 2
        when(varasto.saldo(2)).thenReturn(0); 
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipä", 6));

        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // asiointi
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(5));
    }
    
    @Test
    public void aloitaAsiointiNollaaEdellisenOstoksenTiedot() {
        when(viite.uusi()).thenReturn(42);
        
        // tuote 1
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        
        // tuote 2
        when(varasto.saldo(2)).thenReturn(3); 
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipä", 6));

        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // asiointi
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("pekka", "12345");
        // toinen ostoskierros
        when(viite.uusi()).thenReturn(43);
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("kalle", "23456");
        
        verify(pankki).tilisiirto(eq("kalle"), eq(43), eq("23456"), eq("33333-44455"), eq(5));
    }
    
    @Test
    public void kauppaPyytaaUudenViitenumeronJokaiselleMaksutapahtumalle() {
        when(viite.uusi()).thenReturn(42);
        
        // tuote 1
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        
        // tuote 2
        when(varasto.saldo(2)).thenReturn(3); 
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "leipä", 6));

        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // asiointi
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("pekka", "12345");
        // toinen ostoskierros
        when(viite.uusi()).thenReturn(43);
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("kalle", "23456");
        // kolmas ostoskierros
        when(viite.uusi()).thenReturn(44);
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("julia", "34567");

        verify(viite, times(3)).uusi();
    }
    
    @Test
    public void poistaKoristaPoistaaTuotteenKorista() {
        when(viite.uusi()).thenReturn(42);
        
        // tuote 1
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        Kauppa k = new Kauppa(varasto, pankki, viite);              

        // asiointi
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.poistaKorista(1);
        k.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(0));
    }
}

