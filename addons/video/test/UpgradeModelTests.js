UpgradeModelTests = TestCase("[Video] Upgrade Model Video");

UpgradeModelTests.prototype.setUp = function() {
    this.presenter = Addonvideo_create();

    this.model = {
        "Files": [{
            "Ogg video": "",
            "MP4 video": "",
            "WebM video": "",
            "Subtitles": "",
            ID : ""
        }],
        "Show video": ""
    };

};

UpgradeModelTests.prototype.testUpgradePoster = function() {
    var expectedFilesAfterUpgradePoster = [{
        "Ogg video": "",
        "MP4 video": "",
        "WebM video": "",
        "Subtitles": "",
        "Poster": "",
        ID : ""
    }];

    var upgradedModel = this.presenter.upgradePoster(this.model);

    assertEquals(expectedFilesAfterUpgradePoster, upgradedModel["Files"]);
    assertNotEquals(this.model, upgradedModel); // Ensure that changes were made on copy
};


UpgradeModelTests.prototype.testUpgradeToCurrentVersion = function() {
    var expectedModel = {
        "Files": [{
            "Ogg video": "",
            "MP4 video": "",
            "WebM video": "",
            "Subtitles": "",
            "Poster": "",
            ID : ""
        }],
        "Show video": ""
    };

    var upgradedModel = this.presenter.upgradeModel(this.model);

    assertEquals(expectedModel, upgradedModel);
    assertNotEquals(this.model, upgradedModel);
};
