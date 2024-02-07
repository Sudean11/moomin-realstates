package com.academicproject.moomin.realstates.service.impl;

import com.academicproject.moomin.realstates.controller.ImageController;
import com.academicproject.moomin.realstates.entity.Location;
import com.academicproject.moomin.realstates.entity.Property;
import com.academicproject.moomin.realstates.entity.PropertyTypes;
import com.academicproject.moomin.realstates.entity.dtos.requestDto.PropertyRequestDto;
import com.academicproject.moomin.realstates.repo.LocationRepo;
import com.academicproject.moomin.realstates.repo.PropertyRepo;
import com.academicproject.moomin.realstates.service.PropertyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.IOException;
import java.util.*;

@Service
public class PropertyServiceImpl implements PropertyService {
    private final ModelMapper modelMapper;
    @Autowired
    PropertyRepo propertyRepo;
    @Autowired
    LocationRepo locationRepo;
    private final ImgurService imgurService;
    @Autowired
    ImageController imageController;
    PropertyServiceImpl(PropertyRepo propertyRepo, ImgurService imgurService, ModelMapper modelMapper){
        this.propertyRepo = propertyRepo;
         this.imgurService = imgurService;
        this.modelMapper = modelMapper;
    }


    public List<Property> findAll(String type, String area, String zip, String state, String city) {
        Specification<Property> specification = Specification.where(null);

        if (!type.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("type"), "%" + type + "%"));
        }

        if (!area.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("area"), "%" + area + "%"));
        }

        if (!zip.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("location").get("zipCode"), "%" + zip + "%"));
        }

        if (!state.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("location").get("state"), "%" + state + "%"));
        }

        if (!city.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("location").get("city"), "%" + city + "%"));
            System.out.println(specification);

        }


        return propertyRepo.findAll(specification);
    }
    @Override
    public void save(PropertyRequestDto propertyDto) {
        String bannerLink = null;
        MultipartFile banner = propertyDto.getBanner();
        if (banner != null && !banner.isEmpty()) {
            try {
                byte[] imageData = banner.getBytes();
                String imgurResponse = imgurService.uploadImage(imageData);
                bannerLink = imageController.extractImageLink(imgurResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> imageLinks = new ArrayList<>();
        List<MultipartFile> propertyImages = propertyDto.getPropertyImages();
        if (propertyImages != null && !propertyImages.isEmpty()) {
            for (MultipartFile image : propertyImages) {
                if (image != null && !image.isEmpty()) {
                    try {
                        byte[] imageData = image.getBytes();
                        String imgurResponse = imgurService.uploadImage(imageData);
                        String imageLink = imageController.extractImageLink(imgurResponse);
                        imageLinks.add(imageLink);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Property property = modelMapper.map(propertyDto, Property.class);
        property.setBanner(bannerLink);
        property.setPropertyImages(imageLinks);

        Location location = property.getLocation();

        if (location != null && location.getId() == null) {
            locationRepo.save(location);
        }
    propertyRepo.save(property);
 }



    @Override
    public Optional<Property> findById(Long id) {
        return  propertyRepo.findById(id);

    }

    @Override
    public void deleteById(Long id) {
        Optional<Property> propertyOptional = propertyRepo.findById(id);
        propertyOptional.ifPresent(property -> {
            Location location = property.getLocation();
            if (location != null) {
                location.getProperties().remove(property);
                locationRepo.save(location);
            }
            propertyRepo.deleteById(id);
        });
    }


    @Override
    public void update(Property property) {
        propertyRepo.save(property);

    }


    @Override
    public List<Property> getFeaturedProperty() {
        return propertyRepo.getFeaturedProperty();
    }

    @Override
    public List<Property> getRecentProperty() {
        return propertyRepo.findLast8AddedProperties((Pageable) PageRequest.of(0, 8));
    }

    @Override
    public List<Property> findByCategory(PropertyTypes category) {
    return propertyRepo.findByCategoryWithLocation(category);
    }

    @Override
    public Integer findCount(PropertyTypes category) {
        return propertyRepo.countByCategory(category);
    }

}
