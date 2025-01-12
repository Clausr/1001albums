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
            "https://i.scdn.co/image/c61eee6e4322b187faf78e94b72979d831a60f4e",
            "https://i.scdn.co/image/58e94705a7a411bc4e91b8298a454471135f0dd3",
            "https://i.scdn.co/image/f7f22420531d4850aa5b568445feaa9b62af60e7",
            "https://i.scdn.co/image/bad0306a070fb9b09a0b9ee40872ed566e23d61f",
            "https://i.scdn.co/image/940cb8bad50269081a3c0bca7286e8ff5a5d84d5",
            "https://i.scdn.co/image/c1fabe2e86e55234ed060ff5b0cdd8e3ab896560",
            "https://i.scdn.co/image/40ef7201f871c806d312f2e9375ec68b28b61522",
            "https://i.scdn.co/image/9a251a9ea3e80d65d8e41a05823295fb74fec088",
            "https://i.scdn.co/image/84ca0f9564ae1eff9163a6a1dfae2a10b1d60a6a",
            "https://i.scdn.co/image/bc371b6b1115b50daf92dd2f6e211dc3aeff235e",
            "https://i.scdn.co/image/ab67616d00001e0290a50cfe99a4c19ff3cbfbdb",
            "https://i.scdn.co/image/4f399666325158516073a0bdeebeded9ebb6893d",
            "https://i.scdn.co/image/adb304d21869a2765f55ad2d58c0d314cfe984be",
            "https://i.scdn.co/image/86a5fb6d39a889db62b5a8d19c6ca97307962f2c",
            "https://i.scdn.co/image/400dee6165a81c49b665d18637e3954213679ee8",
            "https://i.scdn.co/image/ab67616d00001e021311a92b0ca83a5154c5a5e7",
            "https://i.scdn.co/image/9f4688be9768a6f6d799fba26479b54746361fe2",
            "https://i.scdn.co/image/acb6fa806b845764a43775310e4378d2c9503f5f",
            "https://i.scdn.co/image/ab67616d00001e02f34338e9270f9c0561cf4c7e",
            "https://i.scdn.co/image/a482bfa14e61812b58f8d0fc3891e6654d68078d",
            "https://i.scdn.co/image/14f52d613a1f0525e2bc213388496b6173112088",
            "https://i.scdn.co/image/ab67616d00001e022021e21bc55e928b744a0113",
            "https://i.scdn.co/image/ab67616d00001e02945696d01c650eeade335ac9",
            "https://i.scdn.co/image/ea708240bb0d63f19668674bddaf01be234eb477",
            "https://i.scdn.co/image/90bbda34115c2d2fd6254ffeec865232bbcf92ab",
            "https://i.scdn.co/image/0ee7dd0ad4d5f60c647a0640f3861ea56db30626",
            "https://i.scdn.co/image/dadfde02fbaad58333506932da8413b9f76675f9",
            "https://i.scdn.co/image/ab67616d00001e025aa1262c4123fedc2e4b8c44",
            "https://i.scdn.co/image/afac90c8979a0e93db3b6c58ac18f5899ee7f218",
            "https://i.scdn.co/image/86186b5082c70be197dca2163e2f1c97d1be7104",
            "https://i.scdn.co/image/ab67616d00001e026f2f499c1df1f210c9b34b32",
            "https://i.scdn.co/image/742fcdea16c23b2335b2ea655250831dfff22b84",
            "https://i.scdn.co/image/ab67616d00001e027ab89c25093ea3787b1995b4",
            "https://i.scdn.co/image/f6f134b51b8e88dc4be6bef65613a2142113a3e6",
            "https://i.scdn.co/image/065142e0c4c0cb194e20702cae8dda05d1085a44",
            "https://i.scdn.co/image/1b2d3105a3e87cc9fb011dacc19cbdc9c678d803",
            "https://i.scdn.co/image/7dc09be2030c957d4c4e0a00e2880aeefd42093b",
            "https://i.scdn.co/image/ab67616d00001e024a04593b7c149dc7b725683e",
            "https://i.scdn.co/image/dee867bbe7a3dd97a37212de86cd40166ebf6dc5",
            "https://i.scdn.co/image/42bb4e98900ff6cbf1968f2908a1dff1754d67f4",
            "https://i.scdn.co/image/ab67616d00001e026aa9314b7ddfbd8f036ba3ac",
            "https://i.scdn.co/image/ab67616d00001e0261834aa14b97a7d9c693134f",
            "https://i.scdn.co/image/ab67616d00001e0223350feac07f56d8b96f33d5",
            "https://i.scdn.co/image/66fd30da44cd9a21d402513f1ed0edf5e1a77c4b",
            "https://i.scdn.co/image/76f5b86a326ca21f892ebaa5f77e76968e6ca3ba",
            "https://i.scdn.co/image/ab67616d00001e020b51f8d91f3a21e8426361ae",
            "https://i.scdn.co/image/2be7232708f6959da59818238050cfd2c4982d54",
            "https://i.scdn.co/image/ab67616d00001e02858ed9e2832801189187391a",
            "https://i.scdn.co/image/ab67616d00001e0262e97ae5072de10850578af5",
            "https://i.scdn.co/image/4b9994763bc8efbd74bd6b6429e111ad167523b7",
        )
    }
}
