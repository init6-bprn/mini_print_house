package ru.bprn.printhouse.views.templates.service;

import org.reflections.Reflections;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.annotation.MenuItem;
import ru.bprn.printhouse.views.templates.entity.TemplatesMenuItem;
import ru.bprn.printhouse.views.templates.repository.TemplatesMenuItemRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TemplatesMenuItemService {

    private final TemplatesMenuItemRepository repository;

    public TemplatesMenuItemService(TemplatesMenuItemRepository repository){
        this.repository = repository;
    }

    public List<TemplatesMenuItem> getMenuByContext(String context) {
        return repository.findByContextAndActiveTrueOrderByOrderIndex(context);

    }

    public void scanAndUpdateMenuItems(String path) {
        Reflections reflections = new Reflections(path);

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(MenuItem.class);

        for (Class<?> clazz : annotated) {
            String className = clazz.getName();
            if (repository.existsByClassName(className)) {
                continue; // уже есть
            }

            MenuItem annotation = clazz.getAnnotation(MenuItem.class);

            TemplatesMenuItem item = TemplatesMenuItem.builder()
                    .name(annotation.name())
                    .className(className)
                    .icon(annotation.icon().name())
                    .context(annotation.context())
                    .active(true)
                    .orderIndex(999) // по умолчанию в конец
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            repository.save(item);
        }
    }
}
