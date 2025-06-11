package ru.bprn.printhouse.views.templates.service;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.repository.ChainsRepository;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;

import java.util.*;

@Service
public class TemplatesService {

    private final TemplatesRepository repository;
    private final ChainsRepository chainsRepository;

    public TemplatesService (TemplatesRepository repository, ChainsRepository chainsRepository) {
        this.repository = repository;
        this.chainsRepository = chainsRepository;
    }

    public List<Templates> findAll() {return this.repository.findAll();}

    public List<Chains> findAllChains(String filter) {
        if (filter==null||filter.isEmpty()) return this.chainsRepository.findAll();
        else return this.chainsRepository.search(filter);}

    public List<AbstractTemplate> findAllAsAbstractTemplates() {
        List<AbstractTemplate> aList = new ArrayList<>();
        for (Templates t : this.repository.findAll())
            aList.add((AbstractTemplate) t);
        return aList ;
    }

    public void delete(Templates templates) {this.repository.delete(templates);}

    public Templates save(Templates templates) {return  this.repository.save(templates);}

    public void saveAndFlush(Templates templates) {this.repository.saveAndFlush(templates);}

    public Optional<Templates> findById(Long id) {return this.repository.findById(id);}

    public Set<Chains> getChainsForTemplate(Templates templates) {
        var temp = findById(templates.getId());
        return temp.map(Templates::getChains).orElse(null);
    }

    public Chains duplicateChain(Chains chain) {
        var newChain = new Chains();
        newChain.setName(chain.getName());
        newChain.setStrJSON(chain.getStrJSON());
        chainsRepository.save(newChain);
        return newChain;
    }

    public void duplicateTemplate(Templates template){
        var newTemplate = new Templates();
        newTemplate.setDescription(template.getDescription());
        newTemplate.setName(template.getName()+" - Дубликат");
        Set<Chains> set = new HashSet<>();
        for (Chains c:template.getChains()) {
            var dc = this.duplicateChain(c);
            if (dc != null) set.add(dc);
        }
        newTemplate.setChains(set);
        this.save(newTemplate);
    }

    public TreeDataProvider<AbstractTemplate> populateGrid(String filter) {
        Collection<AbstractTemplate> collection;
        if (filter == null || filter.isEmpty())
            collection = this.findAllAsAbstractTemplates();
        else collection = this.repository.search(filter);
        TreeData<AbstractTemplate> data = new TreeData<>();
        data.addItems(null, collection);
        for (AbstractTemplate temp : collection) {
            if (temp instanceof Templates) {
                Templates t = (Templates) temp;
                Collection<AbstractTemplate> c = new ArrayList<>(t.getChains());
                data.addItems(temp, c);
            }
        }
        return new TreeDataProvider<>(data);
    }

}
