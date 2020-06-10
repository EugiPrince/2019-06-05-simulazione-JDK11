package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private EventsDao dao;
	private Graph<Integer, DefaultWeightedEdge> grafo;
	private List<Integer> distretti;
	
	public Model() {
		this.dao = new EventsDao();
	}
	
	public List<Integer> getAnni() {
		return this.dao.getAnni();
	}
	
	public void creaGrafo(Integer anno) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		this.distretti = this.dao.getDistretti();
		Graphs.addAllVertices(this.grafo, this.distretti); //Aggiungo tutto tanto so che sono esatti
		
		//SAPENDO CHE SONO POCHI I VERTICI, USO DOPPIO CICLO FOR
		for(Integer v1 : this.grafo.vertexSet()) {
			for(Integer v2 : this.grafo.vertexSet()) {
				if(!v1.equals(v2)) { //Aggiungo l'arco solo se i vertici sono diversi
					if(this.grafo.getEdge(v1, v2) == null) {
						Double latMediaV1 = dao.getLatMedia(anno, v1);
						Double latMediaV2 = dao.getLatMedia(anno, v2);
						
						Double lonMediaV1 = dao.getLonMedia(anno, v1);
						Double lonMediaV2 = dao.getLonMedia(anno, v2);
						
						Double distanzaMedia = LatLngTool.distance(new LatLng(latMediaV1, lonMediaV1),
								new LatLng(latMediaV2, lonMediaV2), LengthUnit.KILOMETER);
						
						Graphs.addEdgeWithVertices(this.grafo, v1, v2, distanzaMedia);
					}
				}
			}
		}
		
		System.out.println("Grafo creato:\n");
		System.out.println("Numero vertici: "+this.grafo.vertexSet().size()+"\n");
		System.out.println("Numero archi: "+this.grafo.edgeSet().size()+"\n");
	}
	
	public List<Vicino> getVicini(Integer distretto) {
		List<Vicino> vicini = new ArrayList<>();
		List<Integer> viciniId = Graphs.neighborListOf(this.grafo, distretto);
		
		for(Integer v : viciniId) {
			vicini.add(new Vicino(v, this.grafo.getEdgeWeight(this.grafo.getEdge(distretto, v))));
		}
		Collections.sort(vicini);
		
		return vicini;
	}

	public Set<Integer> getVertici() {
		return this.grafo.vertexSet();
	}
	
	public Integer simula(Integer anno, Integer mese, Integer giorno, Integer N) {
		Simulatore sim = new Simulatore();
		sim.init(N, anno, mese, giorno, this.grafo);
		return sim.run();
	}

	public List<Integer> getMesi() {
		return this.dao.getMesi();
	}

	public List<Integer> getGiorni() {
		return this.dao.getGiorni();
	}
}
