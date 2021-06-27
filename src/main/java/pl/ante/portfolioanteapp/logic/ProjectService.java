package pl.ante.portfolioanteapp.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.ante.portfolioanteapp.model.*;
import pl.ante.portfolioanteapp.model.projection.ProjectSimpleInfoFactory;
import pl.ante.portfolioanteapp.model.projection.ProjectSimpleInfoReadModel;
import pl.ante.portfolioanteapp.model.projection.ProjectSimpleInfoReadModelPL;
import pl.ante.portfolioanteapp.model.projection.ProjectWriteModel;

import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //---POST/PUT
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
        result.setImages(applyImages(projectWriteModel));

        return projectRepository.save(result);
    }
    public void updateProjectFromWriteModel(final Project project, final ProjectWriteModel projectWriteModel) {
        project.setNamePl(projectWriteModel.getNamePl());
        project.setNameEn(projectWriteModel.getNameEn());
        project.setYear(Year.of(projectWriteModel.getYear()));
        project.setMonth(Month.of(projectWriteModel.getMonth()));
        project.setCityPl(projectWriteModel.getCityPl());
        project.setCityEn(projectWriteModel.getCityEn());
        project.setCountryPl(projectWriteModel.getCountryPl());
        project.setCountryEn(projectWriteModel.getCountryEn());
        project.setClient(projectWriteModel.getClient());
        project.setIcoPath(projectWriteModel.getIcoPath());
        project.setTypes(applyTypes(projectWriteModel));
        project.setImages(applyImages(projectWriteModel));
        projectRepository.save(project);
    }
    private List<Type> applyTypes(final ProjectWriteModel projectWriteModel) {

        List<Type> types = new ArrayList<>();

        projectWriteModel.getTypes().stream()
                .forEach(typeId -> {
                    if(typeRepository.findById(typeId).isEmpty()) {
                        logger.error("There's no such Type when project creation");
                        throw new IllegalArgumentException("There's no such Type");
                    }
                    else types.add(typeRepository.findById(typeId).get());
                });

        return types;
    }
    private Set<ProjectImage> applyImages(final ProjectWriteModel projectWriteModel) {
        return projectWriteModel.getImages().stream()
                .map(imageInfo -> new ProjectImage(imageInfo.getPath(), imageInfo.getBig(), imageInfo.getOrder()))
                .collect(Collectors.toSet());
    }

    //---GET
    public List<ProjectSimpleInfoReadModel> createSortedListOfProjectsByType(final String lang, final Integer typeAsInt) {

        //bad category
        if (isTypeNotValid(typeAsInt)) {
            return createListOfProjectsReadModelsInSpecifiedLangAndOrder(projectRepository.findAll(), lang);
        }






        //-----------------------------------------
//        Type foundType = typeRepository.findById(typeAsInt).get();
//
//        List<Project> projectListByCategory = new ArrayList<>();
//
//        List<Project> allProjectsList = projectRepository.findAll();
//
//        for (Project project : allProjectsList) {
//
//            List<Type> typesOfProject = project.getTypes();
//
//            for (Type type : typesOfProject) {
//                if (type.equals(foundType)) {
//                    projectListByCategory.add(project);
//                }
//            }
//
//        }
//
//        return createListOfProjectsReadModelsInSpecifiedLangAndOrder(projectListByCategory, lang);
        //-----------------------------------------



        //good category
        Type foundType = typeRepository.findById(typeAsInt).get();

        List<Project> projectListByType = projectRepository.findAll().stream()
                .filter(project -> {
                    for (Type type : project.getTypes()) {
                        if(type.equals(foundType)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        return createListOfProjectsReadModelsInSpecifiedLangAndOrder(projectListByType, lang);

    }








    //---utils
    private List<ProjectSimpleInfoReadModel> createListOfProjectsReadModelsInSpecifiedLangAndOrder(
            final List<Project> projects,
            final String lang) {

        //language is not valid
        if (langIsNotPlOrEn(lang)) {
            logger.info("Null or bad language desired - only PL and EN are expected - populated by default PL list");

            return projects.stream()
                    .sorted(Comparator.comparing(Project::getYear).thenComparing(Project::getMonth).thenComparing(Project::getId).reversed())
                    .map(ProjectSimpleInfoReadModelPL::new)
                    .collect(Collectors.toList());
        }

        //language is valid
        return projects.stream()
                .sorted(Comparator.comparing(Project::getYear).thenComparing(Project::getMonth).thenComparing(Project::getId).reversed())
                .map(project -> {
                    ProjectSimpleInfoReadModel result = ProjectSimpleInfoFactory.getInstance().getProjectSimpleInfoReadModel(lang, project);
                    return result;
                })
                .collect(Collectors.toList());
    }


    private boolean langIsNotPlOrEn(final String lang) {
        return lang == null || !lang.toUpperCase().equals("PL") && !lang.toUpperCase().equals("EN");
    }
    private boolean isTypeNotValid(final Integer category) {
        return category == null || category < 1 || category > typeRepository.findAll().size();
    }
}