package org.acme.person.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.enterprise.event.Observes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.acme.person.model.DataTable;
import org.acme.person.model.EyeColor;
import org.acme.person.model.Person;
import org.acme.person.CuteNameGenerator;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


@Path("/person")
public class PersonResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Person>> getAll() {
        return Person.listAll();
    }
    
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello Quarkus Application";
    }

    @GET
    @Path("/eyes/{color}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Person>> findByColor(@PathParam("color") EyeColor color) {
        return Person.findByColor(color);
    }
    @GET
    @Path("/birth/before/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Person> getBeforeYear(@PathParam("year") int year) {
        return Person.getBeforeYear(year);
    }

    @GET
    @Path("/datatable")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DataTable> datatable(@QueryParam("draw") int draw, @QueryParam("start") int start, @QueryParam("length") int length, @QueryParam("search[value]") String searchVal) {
      // TODO: Construct query
       int pageNumber = start / length;
      PanacheQuery<Person> filteredPeople = Optional.ofNullable(searchVal)
          .filter(val -> !val.isEmpty())
          .map(val -> Person.<Person>find("name like :search", Parameters.with("search", "%" + val + "%")))
          .orElseGet(() -> Person.findAll())
          .page(pageNumber, length);
      // TODO: Execute pipeline
         return filteredPeople.list()
            .map(people -> {   // Convert List<Person> to DataTable
                DataTable result = new DataTable();
                result.setDraw(draw);
                result.setData(people);
                return result;
            })
            .flatMap(datatable -> Person.count().map(recordsTotal -> {   // Get the total records count
                datatable.setRecordsTotal(recordsTotal);
                return datatable;
            }))
            .flatMap(datatable -> filteredPeople.count().map(recordsFilteredCount -> {   // Get the number of records filtered
                datatable.setRecordsFiltered(recordsFilteredCount);
                return datatable;
        }));
    }

    void onStart(@Observes StartupEvent ev) {
        for (int i = 0; i < 1000; i++) {
            String name = CuteNameGenerator.generate();
            LocalDate birth = LocalDate.now().plusWeeks(Math.round(Math.floor(Math.random() * 40 * 52 * -1)));
            EyeColor color = EyeColor.values()[(int)(Math.floor(Math.random() * EyeColor.values().length))];
            Person p = new Person();
            p.birth = birth;
            p.eyes = color;
            p.name = name;
            Person.persist(p);
        }
    }

     
}
