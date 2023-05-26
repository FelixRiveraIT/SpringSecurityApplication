package com.example.springsecurityapplication.services;

import com.example.springsecurityapplication.models.Image;
import com.example.springsecurityapplication.models.Product;
import com.example.springsecurityapplication.repositories.ImageRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional (readOnly = true)
public class ImageService {
    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public void deleteImageByProductId (int id){
        imageRepository.deleteImageByProductId(id);
    }

    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    public Image getImageById(Long id) {
        return imageRepository.findById(Math.toIntExact(id)).orElse(null);
    }
}

