'use client';

import { useState } from 'react';
import { languages } from '../../i18n/settings';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

export function LanguageSelector({
    params: { locale },
    ...rest
} : {
    params: { locale: string }
}) {
    const [open, setOpen] = useState(false);

    const pathName = usePathname().split('/').slice(2).join('/');

    return (
        <div {...rest}>
            <span
                className='material-symbols-rounded text-center cursor-pointer select-none translate-y-1'
                onClick={()=>{setOpen(!open)}}
            >language</span>

            {open && (
                <div className="absolute w-max flex flex-col bg-secondary rounded-lg py-2 px-1 right-2">
                    <Link
                        className="hover:bg-primary hover:text-black rounded-md py-1 px-2"
                        href={`/en/${pathName}`}
                    >
                        English
                    </Link>
                    <Link 
                        className="hover:bg-primary hover:text-black rounded-md py-1 px-2 mt-1"
                        href={`/de/${pathName}`}
                    >
                        Deutsch
                    </Link>
                </div>
            )}
        </div>
    )
}