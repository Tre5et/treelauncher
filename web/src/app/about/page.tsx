import Image from "next/image";

export default function Page() {
    return (
        <div className="flex flex-col items-center w-full">
            <div className="flex flex-col items-center p-4 text-center max-w-2xl">
                <p className="text-3xl">About TreeLauncher:</p>
                <p className="text-xl mt-2">In Short:</p>
                <p>TreeLauncher is a modern minecraft launcher that introduces the concept of components for managing instances to streamline instance management and creation.</p>
                <p className="text-xl mt-2">The &quot;Team&quot;:</p>
                <p>TreSet (Planning, Development, Design and everything else)</p>
                <p className="text-xl mt-2">Issues and Feedback:</p>
                <p>Report issues on the <a href="https://github.com/tre5et/treelauncher/issues">GitHub Issues</a> page.</p>
                <p className="text-xl mt-2">Contributing:</p>
                <p>Fork the project on <a href="https://github.com/tre5et/treelauncher">GitHub</a> and make a pull request.</p>
                <p className="text-xl mt-2">A brief note on Account Handling:</p>
                <p>Any login data is handled by the <a href="https://github.com/HyCraftHD/Minecraft-Authenticator">Minecraft-Authenticator</a> third-party java library.</p>
                <p className="mt-1">The launcher uses the User object provided by Minecraft-Authenticator to authenticate a minecraft session at launch. Skin data is retrieved from the official <a href="https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape">Mojang API</a>.</p>
                <p className="mt-1">The secrets file provided by Minecraft-Authenticator is saved to the launcher data directory if &quot;Keep logged in&quot; is selected on login. It is deleted when the logout from settings is used. The file is read and used by Minecraft-Authenticator to reauthenticate on each app start.</p>

                <p className="text-3xl mt-6">About Me:</p>
                <p className="text-xl mt-2">I&apos;m TreSet:</p> 
                <div className="flex flex-row items-center gap-2">
                    <p>Socials:</p>
                    <a href="https://github.com/tre5et">
                        <Image 
                            src="/github.svg" 
                            width={20} 
                            height={20} 
                            alt="GitHub profile"
                            className="invert dark:invert-0"
                        />
                    </a>
                    <a href="https://modrinth.com/user/TreSet">
                        <Image 
                            src="/modrinth.svg" 
                            width={20} 
                            height={20} 
                            alt="Modrinth profile"
                            className="invert dark:invert-0"
                        />
                    </a>
                    <a href="https://www.curseforge.com/members/tre5et">
                        <Image 
                            src="/curseforge.svg" 
                            width={20} 
                            height={20} 
                            alt="Curseforge profile"
                            className="invert dark:invert-0"
                        />
                    </a>
                    <a href="https://twitter.com/ThatTreSet">
                        <Image 
                            src="/twitter.svg" 
                            width={20} 
                            height={20} 
                            alt="Twitter (X) profile"
                            className="invert dark:invert-0"
                        />
                    </a>
                </div>
                <p className="text-xl mt-2">Other Projects:</p>
                <p>Minecraft Mods:&nbsp;
                    <a href="https://modrinth.com/mod/ridehud">RideHud</a>&nbsp;
                    <a href="https://modrinth.com/mod/adaptiveview">AdaptiveView</a>&nbsp;
                    <a href="https://modrinth.com/mod/simple-compass">Simple Compass</a>&nbsp;
                    <a href="https://modrinth.com/mod/vanillaconfig">VanillaConfig</a>&nbsp;
                </p>
                <p>Minecraft Server Discord Manager: <a href="https://github.com/Tre5et/mcs-discman">MCS-Discman</a></p>
                <p className="text-xl mt-2">Donate:</p>
            <p>Support me on <a href="https://ko-fi.com/treset">Ko-Fi</a>.</p>
            <p>Funds may not necessarily go towards further development.</p> 
            </div>
        </div>    
    )
}