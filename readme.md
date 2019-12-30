### WhatsSPI
https://www.jianshu.com/p/3a3edbcd8f24
SPI,全称Service Provider Interface,是一种服务发现机制.
它通过在ClassPath路径下的META-INF/services文件夹查找文件,
自动加载文件里所定义的类.

这一机制为很多框架扩展提供了可能,比如在Dubbo,JDBC中都使用了SPI机制.

ServiceLoader:
```java
public final class ServiceLoader<S> implements Interable<S> implements Iterable<S>{
    private static final String PREFIX = "META-INF/services/";
    
    //The class or interface representing the service being loaded
    private final Class<S> service;
    
    //The class loader used to locate ,load, and instantiate providers
    private final ClassLoader loader;
    
    //The access control context taken when the ServiceLoader is created
    private final AccessControlContext acc;
    
    //Cached providers, in instantiation order
    private LinkedHashMap<String,S> providers = new LinkedHashMap<>();
    
    //The current lazy-lookup interator
    private LazyIterator lookupIterator;
    
    /**
    * Clear this loader's provider cache so that all prividers will be
    * reloaded.
    * 
    * After invoking this method, subsequent invocations of the
    * iterator() method will lazily look up and instantiate
    * providers from scratch, just as is done by a newly-created loader.
    * 
    * This method is intended for use in situations in which new providers 
    * can be installed into a running Java virtual machine.
    */
    public void reload(){
        providers.clear();
        lookupIterator = new LazyIterator(service,loader);
    }
    
    private ServiceLoader(Class<S> svc,ClassLoader cl){
        service = Objects.requireNonNull(svc,"Service interface cannot be null");
        loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        reload();
    }
    
    private static void fail(Class<?> service,String msg,Throwable cause)throws ServiceConfigurationError{
        throw new ServiceConfigurationError(service.getName() + ": " +msg,cause);
    }
    
    private static void fail(Class<?> service,String msg)throws ServiceConfigurationError{
        throw new ServiceConfigurationError(service.getName() + ": " +msg);
    }
    
    private static void fail(Class<?> service,URL u,int line,String msg){
    }
    ...
    
    private int parseLine(Class<?> service,URL u,BufferedReader r, int lc,List<String> names)
        throws IOException,ServiceConfigurationError
    {
        String ln = r.readLine();
        if(ln == null){
            return -1;
        }
        int ci = ln.indexOf('#');
        if(ci >= 0) ln = ln.substring(0,ci);
        ln = ln.trim();
        int n = ln.length();
        if(n != 0){
            if((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)){
                fail(service,u,lc,"illegal configuration-file syntax")''
            }
            
            
        }
        
        
        ...
        //Pase the content of the given URL as a provider-configuration file
        private Interator<String> parse(Class<?> service,URL u)
            throws ServiceConfigurationError{
                InputStream in = null;
                BufferedReader r = null;
                ArrayList<String> names = new ArrayList<>();
                try{
                    in = u.openStream();
                    r = new BufferedReader(new InputStreamReader(in,"utf-8"));
                    int lc = 1;
                    while((lc = parseLine(service,u,r,lc,names))>=0);
                }catch(IOException x){
                    fail(service,...);
                }finally{
                    try{
                        if(r!=null)r.close();
                        if(in!=null)in.close();
                    }catch(IOException y){
                        fail(service,...)''
                    }
                }
                return names.iterator();
            }
        }
        ...
        ...
        
        
        private class LazyIterator implements Iterator<S> {
            Class<S> service;
            ClassLoader loader;
            Enumeration<URL> configs = null;
            Iterator<String> pending = null;
            String nextName = null;
            
            private LazyIterator(Class<S> service,ClassLoader loader){
                this.service = service;
                this.loader = loader;
            }
            
            private boolean hasNextService(){
                if(nextName != null){
                    return true;
                }
                if(configs == null){
                    try{
                        String fullName = PREFIX + service.getName();
                        if(loader == null){
                            configs = ClassLoader.getSystemResources(fullName);
                        }else{
                            configs = loader.getResources(fullName);
                        }
                    }catch(IOException x){
                        fail(service,...);
                    }
                }
                while((pending==null) || !pending.hasNext()){
                    if(!configs.hasMoreElements()){
                        return false;
                    }
                    pending = parse(service,configs.nextElement());
                }
                nextName = pending.next();
                return true;
            }
            
            ...
            
            private S nextService(){
                if (!hasNextService()){
                    throw new NoSuchElementException();
                }
                String cn = nextName;
                nextName = null;
                Class<?> c = null;
                try {
                    c = Class.forName(cn,false,loader);
                }catch(ClassNotFoundException x){
                    fail(service,",,,exception msg");
                }
                if(!service.isAssignableFrom(c)){
                    fail(service,"...exception msg");
                }
                try{
                    S p = service.case(c.newInstance());
                    providers.put(cn,p);
                    return p;
                }catch(Throwable x){
                    fail(service,"...msg");
                }
                throw new Error();
            }
            
            public boolean hasNext(){
                if(acc == null){
                    return hasNextService();
                }else{
                    PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>(){
                        public Boolean run(){
                            return hasNextService();
                        }
                    };
                    return AccessController.doPrivileged(action,acc);
                }
            }
            
            public S next(){
                if(acc == null){
                    return nextService();
                }else{
                    PrivilegedAction<S> action = new PrivilegedAction<S>(){
                        public S run(){
                            return nextService();
                        }
                    };
                    return AccessController.doPrivileged(action,acc);
                }
            }
            
            public void remove(){
                throw new UnsupportedOperationException();
            }
        }
        
    }
    
    
    
```