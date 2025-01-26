package dev.mirodil.testing_system.repositories;

import dev.mirodil.testing_system.models.TestEvent;
import org.springframework.data.repository.CrudRepository;

public interface TestEventRepository extends CrudRepository<TestEvent, Long>, GenericRepositoryWithPagination<TestEvent> {
}
