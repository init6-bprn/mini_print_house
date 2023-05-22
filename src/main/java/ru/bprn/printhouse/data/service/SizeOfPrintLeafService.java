package ru.bprn.printhouse.data.service;

import com.helger.css.ECSSUnit;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.repository.SizeOfPrintLeafRepository;

import java.util.List;

@Service
public class SizeOfPrintLeafService {

    private SizeOfPrintLeafRepository sizeOfPrintLeafRepository;

    public SizeOfPrintLeafService (SizeOfPrintLeafRepository sopfRepository) {
        this.sizeOfPrintLeafRepository = sopfRepository;
    }

    public List<SizeOfPrintLeaf> findAll(){
        return sizeOfPrintLeafRepository.findAll();
    }

    public SizeOfPrintLeaf save (SizeOfPrintLeaf sizeOfPrintLeaf){
        return sizeOfPrintLeafRepository.save(sizeOfPrintLeaf);
    }

    public void delete(SizeOfPrintLeaf sizeOfPrintLeaf) {
        sizeOfPrintLeafRepository.delete(sizeOfPrintLeaf);
    }

}
