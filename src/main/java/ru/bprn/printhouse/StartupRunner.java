package ru.bprn.printhouse;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.templates.entity.TemplatesMenuItem;
import ru.bprn.printhouse.views.templates.repository.TemplatesMenuItemRepository;
import ru.bprn.printhouse.views.templates.service.TemplatesMenuItemService;

import java.util.List;

@Component
@AllArgsConstructor
public class StartupRunner implements ApplicationRunner {

    private final TemplatesMenuItemService scanner;
    private final OperationService operationService;
    private final TemplatesMenuItemRepository templatesMenuItemRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 1. Scan for static menu items defined by annotations
        scanner.scanAndUpdateMenuItems("ru.bprn.printhouse.views");

        // 2. Create dynamic menu items from Operation entities
        createDynamicOperationMenuItems();
    }

    private void createDynamicOperationMenuItems() {
        final String DYNAMIC_OPERATIONS_CONTEXT = "operations_context";
        List<Operation> operations = operationService.findAll();

        for (Operation operation : operations) {
            // Check if an item with this name and context already exists
            if (!templatesMenuItemRepository.existsByNameAndContext(operation.getName(), DYNAMIC_OPERATIONS_CONTEXT)) {
                TemplatesMenuItem item = TemplatesMenuItem.builder()
                        .name(operation.getName())
                        .context(DYNAMIC_OPERATIONS_CONTEXT)
                        // Temporary store Operation ID in className for future navigation handling
                        .className(operation.getId().toString())
                        .icon("COG_O") // Default icon for operations
                        .active(true)
                        .orderIndex(operation.getId().variant()) // Or some other logic for ordering
                        .build();

                templatesMenuItemRepository.save(item);
            }
        }
    }
}
