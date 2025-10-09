# QMedia Android Application Package
PRODUCT_PACKAGES += QMedia

#configuration app for AI Director
PRODUCT_PACKAGES += configurationappforaidirector

# Camera2Video Android Application Package
PRODUCT_PACKAGES += Camera2Video

# UMDAdaptor app
PRODUCT_PACKAGES += UMDAdaptor

PRODUCT_COPY_FILES += \
    vendor/qcom/opensource/commonsys/iot-media-app-commonsys/APPS/umdadaptor/privapp-permissions-org.umdadaptor.xml:$(TARGET_COPY_OUT_SYSTEM_EXT)/etc/permissions/privapp-permissions-org.umdadaptor.xml \
