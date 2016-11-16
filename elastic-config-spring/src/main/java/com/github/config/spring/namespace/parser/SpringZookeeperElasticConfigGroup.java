package com.github.config.spring.namespace.parser;

import lombok.Setter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.github.config.group.ZookeeperConfigProfile;
import com.github.config.group.ZookeeperElasticConfigGroup;
import com.github.config.spring.datasource.PlaceholderResolved;
import com.google.common.base.Optional;

/**
 * 使用Spring启动基于Zookeeper的注册中心.
 * 
 * @author ZhangWei
 */
public final class SpringZookeeperElasticConfigGroup extends
		ZookeeperElasticConfigGroup implements BeanFactoryPostProcessor,
		ApplicationContextAware, PriorityOrdered {

	private static final long serialVersionUID = -944560650617189226L;

	@Setter
	private int order = Ordered.HIGHEST_PRECEDENCE;

	private ApplicationContext applicationContext;

	private final SpringZookeeperConfiguration springZookeeperConfigurationDto;

	public SpringZookeeperElasticConfigGroup(
			final SpringZookeeperConfiguration springZookeeperConfigurationDto) {
		super(new ZookeeperConfigProfile(), springZookeeperConfigurationDto
				.getNode());
		this.springZookeeperConfigurationDto = springZookeeperConfigurationDto;

	}

	@Override
	public void postProcessBeanFactory(
			final ConfigurableListableBeanFactory beanFactory) {

		PlaceholderResolved placeholderResolved = PlaceholderResolved.builder()
				.beanFactory(beanFactory).elasticConfig(this).bulid();
		initElasticConfig(beanFactory, placeholderResolved);
		initWithNoDefaulPlaceholderConfigurer(beanFactory, placeholderResolved);

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public void refresh() {

		if (Optional.fromNullable(applicationContext).isPresent()) {

			if (applicationContext.getParent() != null) {
				((AbstractRefreshableApplicationContext) applicationContext
						.getParent()).refresh();
			}

			((AbstractRefreshableApplicationContext) applicationContext)
					.refresh();

		}

	}

	/**
	 * 初始化 ElasticConfig
	 * 
	 * @param beanFactory
	 *            Bean工厂
	 * @param placeholderResolved
	 *            占位符处理类.
	 */
	private void initElasticConfig(
			final ConfigurableListableBeanFactory beanFactory,
			PlaceholderResolved placeholderResolved) {

		this.getConfigProfile()
				.setServerlist(
						placeholderResolved
								.getResolvePlaceholderText(springZookeeperConfigurationDto
										.getServerLists()));
		this.getConfigProfile()
				.setNamespaces(
						placeholderResolved
								.getResolvePlaceholderText(springZookeeperConfigurationDto
										.getNamespace()));
		this.getConfigProfile()
				.setRootNode(
						placeholderResolved
								.getResolvePlaceholderText(springZookeeperConfigurationDto
										.getRootNode()));
		this.getConfigProfile()
				.setVersion(
						placeholderResolved
								.getResolvePlaceholderText(springZookeeperConfigurationDto
										.getVersion()));
		this.getConfigProfile()
				.setNode(
						placeholderResolved
								.getResolvePlaceholderText(springZookeeperConfigurationDto
										.getNode()));
		super.init();
	}

	/**
	 * 解析占位符
	 * 
	 * @param beanFactory
	 *            beanFactory Bean工厂
	 * @param placeholderResolved
	 *            占位符处理类.
	 */
	private void initWithNoDefaulPlaceholderConfigurer(
			final ConfigurableListableBeanFactory beanFactory,
			PlaceholderResolved placeholderResolved) {

		PropertySourcesPlaceholderConfigurer placeHolder = placeholderResolved
				.getPlaceholderMap().get(
						PropertySourcesPlaceholderConfigurer.class
								.getCanonicalName());
		if (placeHolder != null) {
			placeHolder.postProcessBeanFactory(beanFactory);
		}
	}

}
