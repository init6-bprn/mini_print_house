package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.TypeOfPrinter;
import ru.bprn.printhouse.data.repository.TypeOfPrinterRepository;

import java.util.List;
import java.util.logging.Logger;

@Service
public class TypeOfPrinterService {

        private static final Logger LOGGER = Logger.getLogger(TypeOfPrinter.class
                .getName());
        private TypeOfPrinterRepository typeOfPrinterRepository;

        public TypeOfPrinterService(TypeOfPrinterRepository typeOfPrinterRepository) {
            this.typeOfPrinterRepository = typeOfPrinterRepository;
        }

        public List<TypeOfPrinter> findAll() {
            return typeOfPrinterRepository.findAll();
        }

}

