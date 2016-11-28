package com.github.config.spring.datasource.resolve;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Optional;

/**
 * Value注入解析器
 * 
 * @author ZhangWei
 */
@Slf4j
public class AutowiredAnnotationResoved {

	private String requiredParameterName = "required";

	private boolean requiredParameterValue = true;

	private ConfigurableListableBeanFactory beanFactory;

	private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<String, InjectionMetadata>(64);

	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();

	private static final ConcurrentHashMap<String, AutowiredAnnotationResoved> CONTAINER = new ConcurrentHashMap<String, AutowiredAnnotationResoved>(4);

	@SuppressWarnings("unchecked")
	private AutowiredAnnotationResoved(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.autowiredAnnotationTypes.add(Autowired.class);
		this.autowiredAnnotationTypes.add(Value.class);
		try {
			this.autowiredAnnotationTypes.add((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Inject",
					AutowiredAnnotationBeanPostProcessor.class.getClassLoader()));
			log.info("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
		} catch (ClassNotFoundException ex) {
		}
	}

	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.add(autowiredAnnotationType);
	}

	public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
		Assert.notEmpty(autowiredAnnotationTypes, "'autowiredAnnotationTypes' must not be empty");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
	}

	public void setRequiredParameterName(String requiredParameterName) {
		this.requiredParameterName = requiredParameterName;
	}

	public void setRequiredParameterValue(boolean requiredParameterValue) {
		this.requiredParameterValue = requiredParameterValue;
	}

	public PropertyValues postProcessPropertyValues(PropertyValues pvs, final Optional<PropertyDescriptor[]> pds, Object bean, String beanName)
			throws BeansException {

		InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		} catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
		}
		return pvs;
	}

	public void processInjection(Object bean) throws BeansException {
		Class<?> clazz = bean.getClass();
		InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz, null);
		try {
			metadata.inject(bean, null, null);
		} catch (Throwable ex) {
			throw new BeanCreationException("Injection of autowired dependencies failed for class [" + clazz + "]", ex);
		}
	}

	private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
		// Fall back to class name as cache key, for backwards compatibility
		// with custom callers.
		String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					try {
						metadata = buildAutowiringMetadata(clazz);
						this.injectionMetadataCache.put(cacheKey, metadata);
					} catch (NoClassDefFoundError err) {
						throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName()
								+ "] for autowiring metadata: could not find class that it depends on", err);
					}
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
		LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();
		Class<?> targetClass = clazz;

		do {
			LinkedList<InjectionMetadata.InjectedElement> currElements = new LinkedList<InjectionMetadata.InjectedElement>();
			for (Field field : targetClass.getDeclaredFields()) {
				AnnotationAttributes ann = findAutowiredAnnotation(field);
				if (ann != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (log.isWarnEnabled()) {
							log.warn("Autowired annotation is not supported on static fields: " + field);
						}
						continue;
					}
					boolean required = determineRequiredStatus(ann);
					currElements.add(new AutowiredFieldElement(field, required));
				}
			}
			for (Method method : targetClass.getDeclaredMethods()) {
				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
				if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
					continue;
				}
				AnnotationAttributes ann = findAutowiredAnnotation(bridgedMethod);
				if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
					if (Modifier.isStatic(method.getModifiers())) {
						if (log.isWarnEnabled()) {
							log.warn("Autowired annotation is not supported on static methods: " + method);
						}
						continue;
					}
					if (method.getParameterTypes().length == 0) {
						if (log.isWarnEnabled()) {
							log.warn("Autowired annotation should be used on methods with actual parameters: " + method);
						}
					}
					boolean required = determineRequiredStatus(ann);
					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
					currElements.add(new AutowiredMethodElement(method, required, pd));
				}
			}
			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null && targetClass != Object.class);

		return new InjectionMetadata(clazz, elements);
	}

	private AnnotationAttributes findAutowiredAnnotation(AccessibleObject ao) {
		for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
			AnnotationAttributes ann = AnnotatedElementUtils.getAnnotationAttributes(ao, type.getName());
			if (ann != null) {
				return ann;
			}
		}
		return null;
	}

	protected boolean determineRequiredStatus(AnnotationAttributes ann) {
		return (!ann.containsKey(this.requiredParameterName) || this.requiredParameterValue == ann.getBoolean(this.requiredParameterName));
	}

	protected <T> Map<String, T> findAutowireCandidates(Class<T> type) throws BeansException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("No BeanFactory configured - " + "override the getBeanOfType method or specify the 'beanFactory' property");
		}
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
	}

	private void registerDependentBeans(String beanName, Set<String> autowiredBeanNames) {
		if (beanName != null) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
				}
				if (log.isDebugEnabled()) {
					log.debug("Autowiring by type from bean name '" + beanName + "' to bean named '" + autowiredBeanName + "'");
				}
			}
		}
	}

	private Object resolvedCachedArgument(String beanName, Object cachedArgument) {
		if (cachedArgument instanceof DependencyDescriptor) {
			DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
			return this.beanFactory.resolveDependency(descriptor, beanName, null, null);
		} else if (cachedArgument instanceof RuntimeBeanReference) {
			return this.beanFactory.getBean(((RuntimeBeanReference) cachedArgument).getBeanName());
		} else {
			return cachedArgument;
		}
	}

	private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private volatile boolean cached = false;

		private volatile Object cachedFieldValue;

		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			try {
				Object value;
				if (this.cached) {
					value = resolvedCachedArgument(beanName, this.cachedFieldValue);
				} else {
					DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
					desc.setContainingClass(bean.getClass());
					Set<String> autowiredBeanNames = new LinkedHashSet<String>(1);
					TypeConverter typeConverter = beanFactory.getTypeConverter();
					value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
					synchronized (this) {
						if (!this.cached) {
							if (value != null || this.required) {
								this.cachedFieldValue = desc;
								registerDependentBeans(beanName, autowiredBeanNames);
								if (autowiredBeanNames.size() == 1) {
									String autowiredBeanName = autowiredBeanNames.iterator().next();
									if (beanFactory.containsBean(autowiredBeanName)) {
										if (beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
											this.cachedFieldValue = new RuntimeBeanReference(autowiredBeanName);
										}
									}
								}
							} else {
								this.cachedFieldValue = null;
							}
							this.cached = true;
						}
					}
				}
				if (value != null) {
					ReflectionUtils.makeAccessible(field);
					field.set(bean, value);
				}
			} catch (Throwable ex) {
				throw new BeanCreationException("Could not autowire field: " + field, ex);
			}
		}
	}

	private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {

		private final boolean required;

		private volatile boolean cached = false;

		private volatile Object[] cachedMethodArguments;

		public AutowiredMethodElement(Method method, boolean required, PropertyDescriptor pd) {
			super(method, pd);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			if (checkPropertySkipping(pvs)) {
				return;
			}
			Method method = (Method) this.member;
			try {
				Object[] arguments;
				if (this.cached) {
					// Shortcut for avoiding synchronization...
					arguments = resolveCachedArguments(beanName);
				} else {
					Class<?>[] paramTypes = method.getParameterTypes();
					arguments = new Object[paramTypes.length];
					DependencyDescriptor[] descriptors = new DependencyDescriptor[paramTypes.length];
					Set<String> autowiredBeanNames = new LinkedHashSet<String>(paramTypes.length);
					TypeConverter typeConverter = beanFactory.getTypeConverter();
					for (int i = 0; i < arguments.length; i++) {
						MethodParameter methodParam = new MethodParameter(method, i);
						DependencyDescriptor desc = new DependencyDescriptor(methodParam, this.required);
						desc.setContainingClass(bean.getClass());
						descriptors[i] = desc;
						Object arg = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
						if (arg == null && !this.required) {
							arguments = null;
							break;
						}
						arguments[i] = arg;
					}
					synchronized (this) {
						if (!this.cached) {
							if (arguments != null) {
								this.cachedMethodArguments = new Object[arguments.length];
								for (int i = 0; i < arguments.length; i++) {
									this.cachedMethodArguments[i] = descriptors[i];
								}
								registerDependentBeans(beanName, autowiredBeanNames);
								if (autowiredBeanNames.size() == paramTypes.length) {
									Iterator<String> it = autowiredBeanNames.iterator();
									for (int i = 0; i < paramTypes.length; i++) {
										String autowiredBeanName = it.next();
										if (beanFactory.containsBean(autowiredBeanName)) {
											if (beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
												this.cachedMethodArguments[i] = new RuntimeBeanReference(autowiredBeanName);
											}
										}
									}
								}
							} else {
								this.cachedMethodArguments = null;
							}
							this.cached = true;
						}
					}
				}
				if (arguments != null) {
					ReflectionUtils.makeAccessible(method);
					method.invoke(bean, arguments);
				}
			} catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			} catch (Throwable ex) {
				throw new BeanCreationException("Could not autowire method: " + method, ex);
			}
		}

		private Object[] resolveCachedArguments(String beanName) {
			if (this.cachedMethodArguments == null) {
				return null;
			}
			Object[] arguments = new Object[this.cachedMethodArguments.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = resolvedCachedArgument(beanName, this.cachedMethodArguments[i]);
			}
			return arguments;
		}
	}

	public static AutowiredAnnotationResoved getInstance(ConfigurableListableBeanFactory beanFactory) {

		String name = ConfigurableListableBeanFactory.class.getCanonicalName();
		if (CONTAINER.containsKey(name)) {
			return CONTAINER.get(name);
		}
		CONTAINER.putIfAbsent(name, new AutowiredAnnotationResoved(beanFactory));
		return CONTAINER.get(name);
	}

}
