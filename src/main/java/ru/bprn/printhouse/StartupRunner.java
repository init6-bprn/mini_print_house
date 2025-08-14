package ru.bprn.printhouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.bprn.printhouse.views.templates.service.TemplatesMenuItemService;

@Component
public class StartupRunner implements ApplicationRunner {

    @Autowired
    private TemplatesMenuItemService scanner;

    @Override
    public void run(ApplicationArguments args) {
        scanner.scanAndUpdateMenuItems("ru.bprn.printhouse.views");
    }
}

