package dk.clausr.core.ui

data class CoverData(
    val covers: List<String>,
) {
    companion object {
        fun createCoverDataOrDefault(
            minSize: Int = 50,
            externalList: List<String>,
        ): CoverData {
            return CoverData(externalList.takeIf { external -> external.size >= minSize } ?: default)
        }

        fun default(): CoverData = CoverData(default)

        private val default = listOf(
            "https://i.scdn.co/image/ab67616d00001e025aa1262c4123fedc2e4b8c44",
            "https://i.scdn.co/image/afac90c8979a0e93db3b6c58ac18f5899ee7f218",
            "https://i.scdn.co/image/f7f22420531d4850aa5b568445feaa9b62af60e7",
            "https://i.scdn.co/image/940cb8bad50269081a3c0bca7286e8ff5a5d84d5",
            "https://i.scdn.co/image/ab67616d00001e026f2f499c1df1f210c9b34b32",
            "https://i.scdn.co/image/86186b5082c70be197dca2163e2f1c97d1be7104",
            "https://i.scdn.co/image/742fcdea16c23b2335b2ea655250831dfff22b84",
            "https://i.scdn.co/image/ab67616d00001e027ab89c25093ea3787b1995b4",
            "https://i.scdn.co/image/9a251a9ea3e80d65d8e41a05823295fb74fec088",
            "https://i.scdn.co/image/1b2d3105a3e87cc9fb011dacc19cbdc9c678d803",
            "https://i.scdn.co/image/84ca0f9564ae1eff9163a6a1dfae2a10b1d60a6a",
            "https://i.scdn.co/image/ab67616d00001e024a04593b7c149dc7b725683e",
            "https://i.scdn.co/image/bc371b6b1115b50daf92dd2f6e211dc3aeff235e",
            "https://i.scdn.co/image/ab67616d00001e0290a50cfe99a4c19ff3cbfbdb",
            "https://i.scdn.co/image/dee867bbe7a3dd97a37212de86cd40166ebf6dc5",
            "https://i.scdn.co/image/42bb4e98900ff6cbf1968f2908a1dff1754d67f4",
            "https://i.scdn.co/image/ab67616d00001e0261834aa14b97a7d9c693134f",
            "https://i.scdn.co/image/ab67616d00001e0223350feac07f56d8b96f33d5",
            "https://i.scdn.co/image/66fd30da44cd9a21d402513f1ed0edf5e1a77c4b",
            "https://i.scdn.co/image/76f5b86a326ca21f892ebaa5f77e76968e6ca3ba",
            "https://i.scdn.co/image/ab67616d00001e020b51f8d91f3a21e8426361ae",
            "https://i.scdn.co/image/4f399666325158516073a0bdeebeded9ebb6893d",
            "https://i.scdn.co/image/adb304d21869a2765f55ad2d58c0d314cfe984be",
            "https://i.scdn.co/image/86a5fb6d39a889db62b5a8d19c6ca97307962f2c",
            "https://i.scdn.co/image/ab67616d00001e0262e97ae5072de10850578af5",
            "https://i.scdn.co/image/400dee6165a81c49b665d18637e3954213679ee8",
            "https://i.scdn.co/image/4b9994763bc8efbd74bd6b6429e111ad167523b7",
            "https://i.scdn.co/image/ab67616d00001e02858ed9e2832801189187391a",
            "https://i.scdn.co/image/ab67616d00001e021311a92b0ca83a5154c5a5e7",
            "https://i.scdn.co/image/257237fe469affd86b4d18bb3f81ee5ad2839159",
            "https://i.scdn.co/image/ab67616d00001e02409b4acd6ef9f8f05a41466c",
            "https://i.scdn.co/image/329423c0d0435d8a29c91689f48c4b6a3c067575",
            "https://i.scdn.co/image/690e5c2be4147fe16bf810b5d7e4ce3582319945",
            "https://i.scdn.co/image/9ca833f19ec7df74eda25221b5d6abb9af3b4755",
            "https://i.scdn.co/image/755a11cfa36419d006b1482feba5799cbcf1c2d8",
            "https://i.scdn.co/image/e478c9aa6b08b1816ec54f47a1fbfca38875e0aa",
            "https://i.scdn.co/image/940eb7a70b6ff1febf64ec974305b3555d7f7641",
            "https://i.scdn.co/image/730ba683efd452583b60ba1a0b227c5abdf224b8",
            "https://i.scdn.co/image/698e2edd55f41be33769f08906795c2e5b49d48e",
            "https://i.scdn.co/image/ab67616d00001e0298260c528e6eec9dd431c1d7",
            "https://i.scdn.co/image/3bf4c8049d3d088de18ffb2dbdba26873792780b",
            "https://i.scdn.co/image/0bc13a3eb3ecb8c5b5e6b285d0f69af99d92811c",
            "https://i.scdn.co/image/9f4688be9768a6f6d799fba26479b54746361fe2",
            "https://i.scdn.co/image/ab67616d00001e020e36a62897cf3f5937bf9c16",
            "https://i.scdn.co/image/ab67616d00001e029d1d0e64081ea5c0927d6051",
            "https://i.scdn.co/image/65e5cbb5fbbb20385d042ce7aa357309388f555e",
            "https://i.scdn.co/image/ab67616d00001e02f6c46838e4425ea96e2562fe",
            "https://i.scdn.co/image/01244d847df2e994a5474a2a8979715cc8d77a7a",
            "https://i.scdn.co/image/e1a16b17492b091182a76b3cdb32b69232cdea09",
            "https://i.scdn.co/image/669c314b5bbccd288a044a57aa072cd710e93348",
        )
    }
}
