ifneq ($(TARGET_1G_DDR_RAM), true)
# QMedia Android Application Package
PRODUCT_PACKAGES += QMedia

#configuration app for AI Director
PRODUCT_PACKAGES += configurationappforaidirector

# Camera2Video Android Application Package
PRODUCT_PACKAGES += Camera2Video

# UMDAdaptor app
PRODUCT_PACKAGES += UMDAdaptor
endif
