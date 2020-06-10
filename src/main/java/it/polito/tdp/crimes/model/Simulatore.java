package it.polito.tdp.crimes.model;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.crimes.db.EventsDao;
import it.polito.tdp.crimes.model.Evento.TipoEvento;

public class Simulatore {

	//TIPI DI EVENTO
	//1. Evento criminoso
	// 1.1 Controllare se c'è un agente libero più vicino, se non c'è -> crimine mal gestito
	// 1.2 Se c'è, allora l'agente sarà occupato e non potrà servirne altri fino a quando avrà finito
	
	//2. Agente selezionato arriva sul posto
	// 2.1 Definisco quanto dura l'intervento -> scatenerà un altro evento che modella il termine del crimine
	// 2.2 Se l'agente arriva con > 15 min di ritardo -> crimine mal gestito
	
	//3. Crimine terminato
	// 3.1 L'agente rimane in quel distretto ma sarà non più occupato -> torna disponibile
	
	//STRUTTURE DATI
	//Input dell'utente
	private Integer N = 7; //Numero di agenti + valore di default
	private Integer anno;
	private Integer mese;
	private Integer giorno;
	
	//Stato del sistema
	private Graph<Integer, DefaultWeightedEdge> grafo; //Lo usiamo per le distanze che devono percorrere gli agenti
	//Ci serve anche sapere quali sono gli agenti liberi e dove si trovano, faccio una mappa in cui la chiave
	//rappresenta il distretto e il valore rappresenti il numero di agenti attualmente liberi in quel distretto,
	//all'inizio tutti dove è presente la centrale
	private Map<Integer, Integer> agenti;
	
	//Coda degli eventi
	private PriorityQueue<Evento> queue;
	
	//Output
	private Integer malGestiti;
	
	public void init(Integer N, Integer anno, Integer mese, Integer giorno, Graph<Integer, DefaultWeightedEdge> grafo) {
		this.N = N;
		this.anno = anno;
		this.mese = mese;
		this.giorno = giorno;
		this.grafo = grafo;
		
		this.malGestiti = 0;
		this.agenti = new HashMap<>();
		for(Integer d : this.grafo.vertexSet())
			this.agenti.put(d, 0); //Metto 0 agenti in ogni distretto
		
		//Scelgo dove mettere la centrale e mettere tutti gli agenti lì (distretto a minore criminalità nell'anno)
		EventsDao dao = new EventsDao();
		Integer minD = dao.ditrettoMin(anno);
		
		this.agenti.put(minD, N); //A questo punto li metto tutti lì
		
		//Creo la coda
		this.queue = new PriorityQueue<>();
		
		for(Event e : dao.listAllEventsByDate(anno, mese, giorno)) {
			this.queue.add(new Evento(TipoEvento.CRIMINE, e.getReported_date(), e));
		}
	}
	
	public Integer run() {
		Evento e;
		while((e = this.queue.poll()) != null) {
			switch (e.getTipo()) {
			case CRIMINE:
				System.out.println("Nuovo crimine: "+e.getCrimine().getIncident_id());
				//Cerco l'agente libero più vicino
				Integer partenza = null;
				partenza = cercaAgente(e.getCrimine().getDistrict_id()); //In output c'è il distretto ad cui parte l'agente libero se c'è
				//a partire dal distretto in cui c'è un crimine
				
				if(partenza != null) {
					this.agenti.put(partenza, this.agenti.get(partenza)-1); //ne tolgo uno
					//Quanto ci metterà l'agente libero ad arrivare sul posto?
					Double distanza;
					if(partenza.equals(e.getCrimine().getDistrict_id()))
						distanza = 0.0;
					else
						distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(partenza, e.getCrimine().getDistrict_id()));
					
					Long seconds = (long) ((distanza * 1000)/(60/3.6));
					//Schedulo evento di arrivo agente
					this.queue.add(new Evento(TipoEvento.ARRIVA_AGENTE, e.getData().plusSeconds(seconds), e.getCrimine()));
					
				}
				else {
					System.out.println("Crimine "+e.getCrimine().getIncident_id()+" mal gestito");
					this.malGestiti++;
				}
				break;
			case ARRIVA_AGENTE:
				System.out.println("Arriva agente per il crimine: "+e.getCrimine().getIncident_id());
				Long duration = getDurata(e.getCrimine().getOffense_category_id());
				
				this.queue.add(new Evento(TipoEvento.GESTITO, e.getData().plusSeconds(duration), e.getCrimine()));
				//Controllare se il crimine è mal gestito, cioè se è arrivato con più di 15 min di ritardo
				if(e.getData().isAfter(e.getCrimine().getReported_date().plusMinutes(15))) {
					System.out.println("Crimine "+e.getCrimine().getIncident_id()+" mal gestito");
					this.malGestiti++;
				}
				break;
			case GESTITO:
				//Liberare l'agente, rendendolo disponibile ma rimane in quel distretto
				System.out.println("Crimine: "+e.getCrimine().getIncident_id()+" gestito");
				this.agenti.put(e.getCrimine().getDistrict_id(), this.agenti.get(e.getCrimine().getDistrict_id())+1);
				break;
			}
		}
		
		return this.malGestiti;
	}

	private Long getDurata(String offense_category_id) {
		if(offense_category_id.equals("all_other_crimes")) {
			Random r = new Random();
			if(r.nextDouble()>0.5)
				return Long.valueOf(2*60*60);
			else
				return Long.valueOf(60*60);
		}
		else
			return Long.valueOf(2*60*60);
	}

	private Integer cercaAgente(Integer district_id) {
		Double distanza = Double.MAX_VALUE;
		Integer distretto = null; //Valore di ritorno
		
		for(Integer d : this.agenti.keySet()) {
			if(this.agenti.get(d)>0) { //Se ci sono agenti liberi...
				if(district_id.equals(d)) { //Se il distretto è già quello...
					distanza = 0.0;
					distretto = d;
				}
				else if(this.grafo.getEdgeWeight(this.grafo.getEdge(district_id, d)) < distanza){
					distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(district_id, d));
					distretto = d;
				}
			}
		}
		return distretto;
	}
}
