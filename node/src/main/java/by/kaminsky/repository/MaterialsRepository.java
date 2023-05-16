package by.kaminsky.repository;

import by.kaminsky.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialsRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByName(String name);

    Optional<Material> findByNameAndSpecificAndSource(String name, String specific, String source);

    Boolean existsMaterialByNameAndSpecific(String name, String specific);

}
