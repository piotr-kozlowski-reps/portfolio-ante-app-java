package pl.ante.portfolioanteapp.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.ante.portfolioanteapp.exceptions.NoSuchTypeException;
import pl.ante.portfolioanteapp.model.Project;
import pl.ante.portfolioanteapp.model.ProjectRepository;
import pl.ante.portfolioanteapp.model.Type;
import pl.ante.portfolioanteapp.model.TypeRepository;
import pl.ante.portfolioanteapp.model.projection.ProjectWriteModel;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    //---fields
    Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private ProjectRepository projectRepository;
    private TypeRepository typeRepository;


    //---constr
    ProjectService(final ProjectRepository projectRepository, final TypeRepository typeRepository) {
        this.projectRepository = projectRepository;
        this.typeRepository = typeRepository;
    }


    //---methods
    public Project createProjectFromWriteModel(final ProjectWriteModel projectWriteModel) {

        var result = new Project();
        result.setNamePl(projectWriteModel.getNamePl());
        result.setNameEn(projectWriteModel.getNameEn());
        result.setYear(Year.of(projectWriteModel.getYear()));
        result.setMonth(Month.of(projectWriteModel.getMonth()));
        result.setCityPl(projectWriteModel.getCityPl());
        result.setCityEn(projectWriteModel.getCityEn());
        result.setCountryPl(projectWriteModel.getCountryPl());
        result.setCountryEn(projectWriteModel.getCountryEn());
        result.setClient(projectWriteModel.getClient());
        result.setIcoPath(projectWriteModel.getIcoPath());
        result.setTypes(applyTypes(projectWriteModel));
        result.setImages(projectWriteModel.getImages());

        return result;
    }

    private List<Type> applyTypes(final ProjectWriteModel projectWriteModel) {

        List<Type> types = new ArrayList<>();


        try {
            for (Integer typeId : projectWriteModel.getTypes()) {
                Type type = typeRepository.findById(typeId)
                        .orElseThrow(() -> new NoSuchTypeException("There's no such Type"));
                types.add(type);
            }
        } catch (NoSuchTypeException e) {
            logger.error(e.getMessage(), e);
        }


        return types;

    }
}
