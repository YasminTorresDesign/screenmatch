package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosEpisodio;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporadas;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.service.ConsumoApi;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=9fce3c20";
    private ConvierteDatos conversor = new ConvierteDatos();

    public void muestraElMenu() {
        System.out.println("Escribe el nombre de la serie a buscar");


        //busca los datos generales de las series
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        //"https://www.omdbapi.com/?t=Game+of+Thrones&apikey=9fce3c20"
        var datos = conversor.obtenerDatos(json, DatosSerie.class);
        //DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);

        System.out.println(datos);


        //busca los datos de todas las temporadas
        List<DatosTemporadas> temporadas = new ArrayList<>();

        //!deshabilite temporalmente en nombreSerie.replace(" ", "+") me daba error
        for (int i = 1; i <= datos.totaldeTemporadas(); i++) {
            json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + "&Season=" +i+ API_KEY);
            //"https://www.omdbapi.com/?t=Game+of+Thrones&season="+i+"&apikey=9fce3c20"
            var datosTemporadas = conversor.obtenerDatos(json, DatosTemporadas.class);
            //DatosTemporadas datosTemporadas = conversor.obtenerDatos(json, DatosTemporadas.class);
            temporadas.add(datosTemporadas);
        }
        //temporadas.forEach(System.out::println);

        //mostrar el titulo de los episodios de las temporadas
        /*
        for (int i = 0; i < datos.totaldeTemporadas(); i++) {
            List<DatosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }
        */
        //simplificar el codigo de los for i, for j con funciones LAMBDA
        //temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        //Convertir todas las informaciones a una lista DatosEpisodio
        List<DatosEpisodio> datosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        //top 5 de los episodios
        /*System.out.println("Top 5 de Episodios");
        datosEpisodios.stream()
                .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("primer filtro N/A" + e))
                .sorted(Comparator.comparing(DatosEpisodio::evaluacion).reversed())
                .peek(e -> System.out.println("Segundo filtro ordenacion M>m" + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e -> System.out.println("Tercer filtro mayuscula m>M" + e))
                .limit(5)
                .forEach(System.out::println);*/

        //convirtiendo los datos a una lista del tipo Episodio
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(),d)))
                .collect(Collectors.toList());

        //episodios.forEach(System.out::println);

        //busqueda de episodio a partir del año
      /*  System.out.println("Año a partir del cual ver los episodios");
        var fecha = teclado.nextInt();
        teclado.nextLine();*/

        //LocalDate fechaBusqueda = LocalDate.of(fecha, 1, 1);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        /*episodios.stream()
                .filter(e -> e.getFechaDeLanzamiento() != null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
                .forEach(e -> System.out.println(
                        "Temporada: " + e.getTemporada()+
                                " episodio: " + e.getTitulo() +
                                " Fecha de lanzamiento: " + e.getFechaDeLanzamiento().format(dtf)
                ));*/

        //busca episodio por un pedazo del titulo
        /*System.out.println("Escribe el titulo del episodio");
        var pedazoTitulo = teclado.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(pedazoTitulo.toUpperCase()))
                .findFirst();

        if(episodioBuscado.isPresent()) {
            System.out.println("Episodio Encontrado");
            System.out.println("Los datos del episodios son: " + episodioBuscado.get());
        }else {
            System.out.println("Episodio No Encontrado");
        }*/

        //crear mapas de temporada
        Map<Integer, Double> evaluacionesPorTemporada = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0 )//no tiene en cuenta las temporadas sin evaluacion
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getEvaluacion)));
        System.out.println(evaluacionesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getEvaluacion() > 0.0 )
                .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
        System.out.println("Media de las evaluaciones: " + est.getAverage());
        System.out.println("Episodio mejor valorado: " + est.getMax());
        System.out.println("Episodio peor valorado: " + est.getMin());


    }
}
