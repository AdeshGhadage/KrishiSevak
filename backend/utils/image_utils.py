"""
Image processing utilities for plant disease detection
"""

import io
import numpy as np
from PIL import Image, ImageOps, ExifTags
import cv2
from typing import Tuple, Optional
import hashlib

from ..config import settings

def validate_image(image_bytes: bytes) -> Image.Image:
    """
    Validate and load image from bytes
    
    Args:
        image_bytes: Raw image bytes
        
    Returns:
        PIL Image object
        
    Raises:
        ValueError: If image is invalid or corrupted
    """
    try:
        # Load image
        image = Image.open(io.BytesIO(image_bytes))
        
        # Verify image
        image.verify()
        
        # Reload for processing (verify() closes the file)
        image = Image.open(io.BytesIO(image_bytes))
        
        # Convert to RGB if necessary
        if image.mode not in ['RGB', 'L']:
            image = image.convert('RGB')
        
        # Check minimum dimensions
        min_size = 224  # Minimum size for ViT models
        if min(image.size) < min_size:
            raise ValueError(f"Image too small. Minimum size: {min_size}x{min_size}")
        
        # Check maximum dimensions
        max_size = 4096
        if max(image.size) > max_size:
            # Resize if too large
            image.thumbnail((max_size, max_size), Image.Resampling.LANCZOS)
        
        return image
        
    except Exception as e:
        raise ValueError(f"Invalid image: {str(e)}")

def fix_image_orientation(image: Image.Image) -> Image.Image:
    """
    Fix image orientation based on EXIF data
    
    Args:
        image: PIL Image object
        
    Returns:
        Corrected PIL Image object
    """
    try:
        # Get EXIF data
        exif = image._getexif()
        
        if exif is not None:
            # Find orientation tag
            for tag, value in exif.items():
                decoded = ExifTags.TAGS.get(tag, tag)
                if decoded == 'Orientation':
                    if value == 3:
                        image = image.rotate(180, expand=True)
                    elif value == 6:
                        image = image.rotate(270, expand=True)
                    elif value == 8:
                        image = image.rotate(90, expand=True)
                    break
    except:
        # If EXIF processing fails, continue with original image
        pass
    
    return image

def preprocess_image(image: Image.Image, target_size: Tuple[int, int] = (224, 224)) -> np.ndarray:
    """
    Preprocess image for model inference
    
    Args:
        image: PIL Image object
        target_size: Target size for model input
        
    Returns:
        Preprocessed image as numpy array
    """
    
    # Fix orientation
    image = fix_image_orientation(image)
    
    # Convert to RGB if not already
    if image.mode != 'RGB':
        image = image.convert('RGB')
    
    # Resize image
    image = image.resize(target_size, Image.Resampling.LANCZOS)
    
    # Convert to numpy array
    image_array = np.array(image)
    
    # Normalize pixel values to [0, 1]
    image_array = image_array.astype(np.float32) / 255.0
    
    # Normalize using ImageNet statistics (common for ViT models)
    mean = np.array([0.485, 0.456, 0.406])
    std = np.array([0.229, 0.224, 0.225])
    image_array = (image_array - mean) / std
    
    # Add batch dimension
    image_array = np.expand_dims(image_array, axis=0)
    
    return image_array

def enhance_image_quality(image: Image.Image) -> Image.Image:
    """
    Enhance image quality for better disease detection
    
    Args:
        image: PIL Image object
        
    Returns:
        Enhanced PIL Image object
    """
    
    # Convert to OpenCV format
    cv_image = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
    
    # Apply CLAHE (Contrast Limited Adaptive Histogram Equalization)
    lab = cv2.cvtColor(cv_image, cv2.COLOR_BGR2LAB)
    l, a, b = cv2.split(lab)
    
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    l = clahe.apply(l)
    
    enhanced = cv2.merge([l, a, b])
    enhanced = cv2.cvtColor(enhanced, cv2.COLOR_LAB2BGR)
    
    # Apply slight sharpening
    kernel = np.array([[-1, -1, -1],
                      [-1,  9, -1],
                      [-1, -1, -1]])
    enhanced = cv2.filter2D(enhanced, -1, kernel)
    
    # Convert back to PIL
    enhanced_pil = Image.fromarray(cv2.cvtColor(enhanced, cv2.COLOR_BGR2RGB))
    
    return enhanced_pil

def extract_image_features(image: Image.Image) -> dict:
    """
    Extract basic features from image for metadata
    
    Args:
        image: PIL Image object
        
    Returns:
        Dictionary with image features
    """
    
    # Convert to numpy array
    img_array = np.array(image)
    
    # Basic statistics
    features = {
        'width': image.size[0],
        'height': image.size[1],
        'channels': len(img_array.shape),
        'format': image.format,
        'mode': image.mode,
        'mean_brightness': np.mean(img_array),
        'std_brightness': np.std(img_array),
        'aspect_ratio': image.size[0] / image.size[1]
    }
    
    # Color analysis for RGB images
    if image.mode == 'RGB':
        r, g, b = img_array[:, :, 0], img_array[:, :, 1], img_array[:, :, 2]
        features.update({
            'mean_red': np.mean(r),
            'mean_green': np.mean(g),
            'mean_blue': np.mean(b),
            'dominant_color_channel': ['red', 'green', 'blue'][np.argmax([np.mean(r), np.mean(g), np.mean(b)])]
        })
    
    return features

def generate_image_hash(image_bytes: bytes) -> str:
    """
    Generate hash for image deduplication
    
    Args:
        image_bytes: Raw image bytes
        
    Returns:
        SHA256 hash string
    """
    return hashlib.sha256(image_bytes).hexdigest()

def crop_to_plant_region(image: Image.Image) -> Image.Image:
    """
    Automatically crop image to focus on plant region
    (Simplified implementation - can be enhanced with ML-based segmentation)
    
    Args:
        image: PIL Image object
        
    Returns:
        Cropped PIL Image object
    """
    
    # Convert to OpenCV format
    cv_image = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
    
    # Convert to HSV for better plant detection
    hsv = cv2.cvtColor(cv_image, cv2.COLOR_BGR2HSV)
    
    # Define range for green colors (plants)
    lower_green = np.array([35, 40, 40])
    upper_green = np.array([85, 255, 255])
    
    # Create mask for green regions
    mask = cv2.inRange(hsv, lower_green, upper_green)
    
    # Find contours
    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    if contours:
        # Find largest contour (assuming it's the main plant)
        largest_contour = max(contours, key=cv2.contourArea)
        
        # Get bounding rectangle
        x, y, w, h = cv2.boundingRect(largest_contour)
        
        # Add some padding
        padding = 20
        x = max(0, x - padding)
        y = max(0, y - padding)
        w = min(image.size[0] - x, w + 2 * padding)
        h = min(image.size[1] - y, h + 2 * padding)
        
        # Crop image
        cropped = image.crop((x, y, x + w, y + h))
        return cropped
    
    # If no plant region detected, return original image
    return image

def create_image_thumbnail(image: Image.Image, size: Tuple[int, int] = (256, 256)) -> Image.Image:
    """
    Create thumbnail for image preview
    
    Args:
        image: PIL Image object
        size: Thumbnail size
        
    Returns:
        Thumbnail PIL Image object
    """
    
    # Create thumbnail maintaining aspect ratio
    thumbnail = image.copy()
    thumbnail.thumbnail(size, Image.Resampling.LANCZOS)
    
    # Create a square thumbnail with padding if needed
    square_thumb = Image.new('RGB', size, (255, 255, 255))
    
    # Calculate position to center the image
    x = (size[0] - thumbnail.size[0]) // 2
    y = (size[1] - thumbnail.size[1]) // 2
    
    square_thumb.paste(thumbnail, (x, y))
    
    return square_thumb
