package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.repository.WorkFlowRepository;

import java.util.*;

@Service
public class WorkFlowService {

    private final WorkFlowRepository workFlowRepository;

    public WorkFlowService(WorkFlowRepository workFlowRepository){
        this.workFlowRepository = workFlowRepository;
    }

    public List<WorkFlow> findAll() {return this.workFlowRepository.findAll();}

    public WorkFlow save(WorkFlow workFlow) {return this.workFlowRepository.save(workFlow);}

    public void delete (WorkFlow workFlow) {this.workFlowRepository.delete(workFlow);}

}
